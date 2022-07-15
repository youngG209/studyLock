package study.studylock.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.studylock.domain.Account;
import study.studylock.domain.repository.AccountRepository;
import study.studylock.service.LockService;

@Slf4j
@Qualifier("pessimisticLockServiceImpl")
@Service
@RequiredArgsConstructor
public class PessimisticLockServiceImpl implements LockService {

	private final AccountRepository accountRepository;

	@Override
	public int nowTotalAccount(String accountNumber) {
		return accountRepository.findTotalByAccountNumber(accountNumber);
	}

	@Override
	@Transactional
	public int inMoney(String accountNumber, int money) {
		Account account = accountRepository.findByAccountNumberForPessimisticLock(accountNumber);
		if (account != null) {
			account.inMoney(money);
			log.info("결과 잔고 : {}", account);
			return account.getTotal();
		}
		account = accountRepository.save(new Account(accountNumber, money));
		log.info("결과 잔고 : {}", account);
		return account.getTotal();
	}

	@Override
	@Transactional
	public Account outMoney(String accountNumber, int money) {
		Account account = accountRepository.findByAccountNumberForPessimisticLock(accountNumber);
		account.outMoney(money);
		log.info("결과 잔고 : {}", account);
		return account;
	}

	@Transactional
	public int outMoneyByNoLock(String accountNumber, int money) {
		Account account = accountRepository.findByAccountNumber(accountNumber);
		account.outMoney(money);
		log.info("결과 잔고 : {}", account);
		return account.getTotal();
	}

}
