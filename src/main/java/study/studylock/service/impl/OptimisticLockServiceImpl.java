package study.studylock.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.studylock.domain.AccountByVersion;
import study.studylock.domain.repository.AccountByVersionRepository;
import study.studylock.service.LockService;

@Slf4j
@Qualifier("optimisticLockServiceImpl")
@Service
@RequiredArgsConstructor
public class OptimisticLockServiceImpl implements LockService {

	private final AccountByVersionRepository accountByVersionRepository;

	@Override
	public int nowTotalAccount(String accountNumber) {
		return accountByVersionRepository.findTotalByAccountNumber(accountNumber);
	}

	@Override
	@Transactional
	public int inMoney(String accountNumber, int money) {
		AccountByVersion account = accountByVersionRepository.findByAccountNumber(accountNumber);
		if (account != null) {
			account.inMoney(money);
			log.info("in 결과 잔고 : {}", account);
			return account.getTotal();
		}
		account = accountByVersionRepository.save(new AccountByVersion(accountNumber, money));
		log.info("in 결과 잔고 : {}", account);
		return account.getTotal();
	}

	@Override
	@Transactional
	public AccountByVersion outMoney(String accountNumber, int money) {
		AccountByVersion account = accountByVersionRepository.findByAccountNumber(accountNumber);
//		if (account.getVersion())
		account.outMoney(money);
		log.info("out 결과 잔고 : {}", account);
		return account;
	}

}
