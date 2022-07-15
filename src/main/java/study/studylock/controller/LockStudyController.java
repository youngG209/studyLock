package study.studylock.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.studylock.domain.Account;
import study.studylock.domain.AccountByVersion;
import study.studylock.service.LockService;
import study.studylock.service.impl.PessimisticLockServiceImpl;

@Slf4j
@RestController
@RequestMapping("/lock")
public class LockStudyController {

	private final LockService lockService;

	public LockStudyController(
		@Qualifier("optimisticLockServiceImpl") LockService lockService) {
		this.lockService = lockService;
	}

	@GetMapping("/in")
	public String inMoney(String accountNumber, int money) {

		int i = lockService.inMoney(accountNumber, money);

		return "잔금 : " + i;
	}

	@GetMapping("/out")
	public String outMoney(String accountNumber, int money) {

		Account i = lockService.outMoney(accountNumber, money);

		return "잔금 : " + i;
	}

	@GetMapping("/out/version")
	public String outMoneyByVersion(String accountNumber, int money) {

		AccountByVersion i = lockService.outMoney(accountNumber, money);

		return "잔금 : " + i;
	}
}
