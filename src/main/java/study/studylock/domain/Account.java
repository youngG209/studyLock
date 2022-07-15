package study.studylock.domain;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import study.studylock.exception.NotEnoughMoneyException;

@Getter
@NoArgsConstructor
@ToString
@Entity
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long accountId;

	private String accountNumber;

	private int total;

	public Account(String accountNumber, int total) {
		this.accountNumber = accountNumber;
		this.total = total;
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
