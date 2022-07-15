package study.studylock.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.hibernate.StaleStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import study.studylock.domain.Account;
import study.studylock.domain.AccountByVersion;
import study.studylock.domain.repository.AccountByVersionRepository;
import study.studylock.exception.NotEnoughMoneyException;
import study.studylock.service.impl.OptimisticLockServiceImpl;

@SpringBootTest
class OptimisticLockServiceImplTest {

	@Autowired
	private AccountByVersionRepository accountRepository;

	@Autowired
	private OptimisticLockServiceImpl lockService;

	private final static String ACCOUNT_NUMBER = "12345";
	private final static int TOTAL = 100000;

	@BeforeEach
	void setUp() {
		lockService.inMoney(ACCOUNT_NUMBER, TOTAL);
	}

	@AfterEach
	void tearDown() {
		accountRepository.deleteAll();
	}

	@Test
	void nowTotalAccount() {
		Integer total = lockService.nowTotalAccount(ACCOUNT_NUMBER);

		System.out.println("최종 잔고 : " + total);
		assertThat(total).isEqualTo(100000);
	}

	@Test
	void 단건_출금() {
		int out = 10000;
		AccountByVersion money = lockService.outMoney(ACCOUNT_NUMBER, out);

		System.out.println("최종 잔고 : " + money);
		assertThat(money.getTotal()).isEqualTo(90000);
	}

	@Test
	void 다건_출금_예외확인() {
		Throwable throwable = assertThrows(NotEnoughMoneyException.class, () ->
			{
				Account account = new Account();
				int out = 10000;
				for (int i = 0; i < 11; i++) {
					lockService.outMoney(ACCOUNT_NUMBER, out);
				}
			}
		);

		assertThat("잔고가 부족합니다.").isEqualTo(throwable.getMessage());
	}

	@Test
	void 다건_출금_멀티스레드_CompletableFuture() throws InterruptedException {
		int out = 10000;
		AtomicInteger money = new AtomicInteger();

		int excuteLoopCount = 50;
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();
		AtomicInteger count = new AtomicInteger();
		ExecutorService executor = Executors.newFixedThreadPool(5);
		CompletableFuture<AtomicInteger> atomicIntegerCompletableFuture = new CompletableFuture<>();

		long start = System.nanoTime();

		for (int i = 0; i < excuteLoopCount; i++) {
			int finalI = i;
			atomicIntegerCompletableFuture = CompletableFuture.supplyAsync(() -> {
				AccountByVersion accountByVersion = new AccountByVersion();
				try {
					accountByVersion = lockService.outMoney(ACCOUNT_NUMBER, out);
					money.set(accountByVersion.getTotal());
					successCount.getAndIncrement();
					return money;
				} catch (NotEnoughMoneyException e) {
					System.out.println(e.getMessage());
					failCount.getAndIncrement();
					return money;
				} catch (ObjectOptimisticLockingFailureException e) {
					accountByVersion = lockService.outMoney(ACCOUNT_NUMBER, out);
					System.out.println("ObjectOptimisticLockingFailureException 메세지 : " + e.getMessage());
					failCount.getAndIncrement();
					return money;
				} catch (Exception e) {
					System.out.println("Exception 메세지 : " + e.getMessage());
					failCount.getAndIncrement();
					return money;
				} finally {
					System.out.println(
						count.incrementAndGet() + "번째 잔고 : " + accountByVersion + " / 성공 : "
							+ successCount.get() + " / 실패 : " + failCount.get());
				}
			}, executor);
		}

		AtomicInteger join = atomicIntegerCompletableFuture.join();
		long duration = (System.nanoTime() - start) / 1_000_000;
		System.out.println("완료 시간:  " + duration + " msecs");

		System.out.println("최종 잔고 : " + join.get());
		System.out.println("성공 횟수 : " + successCount.get());
		System.out.println("실패 횟수 : " + failCount.get());

		assertThat(join.get()).isEqualTo(0);
//		assertThat(successCount.get()).isEqualTo(10);
	}
}