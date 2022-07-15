package study.studylock.service;

public interface LockService {

	int nowTotalAccount(String accountNumber);

	int inMoney(String accountNumber, int money);

	<T> T outMoney(String accountNumber, int money);

}
