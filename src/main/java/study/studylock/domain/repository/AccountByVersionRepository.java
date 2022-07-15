package study.studylock.domain.repository;

import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import study.studylock.domain.AccountByVersion;

public interface AccountByVersionRepository extends JpaRepository<AccountByVersion, Long> {

	@Query("select h.total from AccountByVersion h where h.accountNumber = :accountNumber")
	int findTotalByAccountNumber(String accountNumber);

	@Lock(LockModeType.OPTIMISTIC)
	AccountByVersion findByAccountNumber(String accountNumber);

}
