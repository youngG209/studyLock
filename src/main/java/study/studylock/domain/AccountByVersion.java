package study.studylock.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import study.studylock.exception.NotEnoughMoneyException;

@Getter
@NoArgsConstructor
@ToString
@Entity
public class AccountByVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long accountId;

	private String accountNumber;

	private int total;

	@Version
	private Long version;

	public AccountByVersion(String accountNumber, int total) {
		this.accountNumber = accountNumber;
		this.total = total;
		this.version = 0L;
	}

	public void inMoney(int in) {
		this.total += in;
	}

	public void outMoney(int out) {
		int remaining = this.total - out;

		if (remaining < 0) {
			throw new NotEnoughMoneyException("잔고가 부족합니다.");
		}
		this.total = remaining;
	}
}
