package com.northbridge.payments.repository;

import com.northbridge.payments.web.dto.PaymentView;
import com.northbridge.common.util.AccountNumbers;
import com.northbridge.common.util.BankDates;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Read side for the customer payment-activity screen. Plain JDBC so the online-banking team can
 * tune the query against the PAYMENT table without touching the JPA model.
 */
@Repository
public class PaymentHistoryDao {

    private static final String BASE_SELECT =
            "SELECT payment_id, type, cif, from_account, payee_id, recipient_email, recipient_phone, amount, memo, "
            + "send_on, status, core_transfer_id, created_at FROM payment WHERE cif = ?";

    private static final String DEFAULT_SORT = "created_at DESC";

    private final JdbcTemplate jdbc;

    public PaymentHistoryDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PaymentView> findHistory(String cif, String status, String sort) {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = '").append(status.toUpperCase()).append("'");
        }
        sql.append(" ORDER BY ").append(sort == null || sort.isEmpty() ? DEFAULT_SORT : sort);
        return jdbc.query(sql.toString(), new Object[]{cif}, ROW_MAPPER);
    }

    private static final RowMapper<PaymentView> ROW_MAPPER = (rs, rowNum) -> {
        PaymentView v = new PaymentView();
        v.setPaymentId(rs.getString("payment_id"));
        v.setType(rs.getString("type"));
        v.setCif(rs.getString("cif"));
        v.setFromAccount(AccountNumbers.mask(rs.getString("from_account")));
        v.setPayeeId(rs.getString("payee_id"));
        String email = rs.getString("recipient_email");
        v.setRecipient(email != null ? email : rs.getString("recipient_phone"));
        v.setAmount(rs.getBigDecimal("amount").setScale(2).toPlainString());
        v.setMemo(rs.getString("memo"));
        v.setSendOn(rs.getDate("send_on") == null ? null : BankDates.formatDate(rs.getDate("send_on")));
        v.setStatus(rs.getString("status"));
        v.setCoreTransferId(rs.getString("core_transfer_id"));
        v.setCreatedAt(BankDates.formatTimestamp(rs.getTimestamp("created_at")));
        return v;
    };
}
