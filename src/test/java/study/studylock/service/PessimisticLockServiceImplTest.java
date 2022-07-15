package study.studylock.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.studylock.domain.Account;
import study.studylock.domain.repository.AccountRepository;
import study.studylock.exception.NotEnoughMoneyException;
import study.studylock.service.impl.PessimisticLockServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;

//@DataJpaTest
@SpringBootTest
class PessimisticLockServiceImplTest {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private PessimisticLockServiceImpl lockService;

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

		assertThat(total).isEqualTo(100000);
	}

	@Test
	void 단건_출금_멀티스레드() {
		int out = 10000;
		Account account = lockService.outMoney(ACCOUNT_NUMBER, out);

		assertThat(account.getTotal()).isEqualTo(90000);
	}

	@Test
	void 다건_출금_예외확인() {
		Throwable throwable = assertThrows(NotEnoughMoneyException.class, () ->
			{
//				Account account = new Account();
				int out = 10000;
				for (int i = 0; i < 11; i++) {
					lockService.outMoney(ACCOUNT_NUMBER, out);
				}
			}
		);

		assertThat("잔고가 부족합니다.").isEqualTo(throwable.getMessage());
	}

	// DEAD LOCK 걸릴 수 있음
	@Test
	void 다건_출금_멀티스레드() throws InterruptedException {
		int out = 10000;
		AtomicInteger money = new AtomicInteger();

		int excuteCount = 20;
		ExecutorService service = Executors.newFixedThreadPool(20);
		CountDownLatch latch = new CountDownLatch(excuteCount);

		for (int i = 0; i < excuteCount; i++) {
			service.execute(() -> {
				try {
					Account account = lockService.outMoney(ACCOUNT_NUMBER, out);
					money.set(account.getTotal());
					System.out.println("잔고 : " + money);
				} catch (NotEnoughMoneyException e) {
					System.out.println(e.getMessage());
				}
				latch.countDown();
			});
		}
		latch.await();

		assertThat(money.get()).isEqualTo(0);
	}

	@Test
	// 완료 시간 (Executor 미사용) : 1~2
	// 완료 시간 (Executor 사용) : 1812565
	//	https://pjh3749.tistory.com/280
	void 다건_출금_멀티스레드_CompletableFuture() {
		int out = 10000;
		AtomicInteger money = new AtomicInteger();

		int excuteLoopCount = 100;
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();
		ExecutorService executor = Executors.newFixedThreadPool(5);
		CompletableFuture<AtomicInteger> atomicIntegerCompletableFuture = new CompletableFuture<>();

		long start = System.nanoTime();

		for (int i = 0; i < excuteLoopCount; i++) {
			atomicIntegerCompletableFuture = CompletableFuture.supplyAsync(() -> {
				try {
					Account account = lockService.outMoney(ACCOUNT_NUMBER, out);
					money.set(account.getTotal());
					System.out.println("잔고 : " + money);
					successCount.getAndIncrement();
					return money;
				} catch (NotEnoughMoneyException e) {
					System.out.println(e.getMessage());
					failCount.getAndIncrement();
					return money;
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
	}
}