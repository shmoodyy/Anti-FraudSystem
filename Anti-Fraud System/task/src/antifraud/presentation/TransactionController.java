package antifraud.presentation;

import antifraud.business.models.suspicious.cards.StolenCardDTO;
import antifraud.business.models.suspicious.cards.StolenCardDomain;
import antifraud.business.models.suspicious.ips.SuspiciousIpDTO;
import antifraud.business.models.suspicious.ips.SuspiciousIpDomain;
import antifraud.business.models.transactions.*;
import antifraud.business.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/antifraud")
public class TransactionController {

    @Autowired
    private final TransactionService transactionService;

    @Autowired
    private final ModelMapper controllerModelMapper;

    @PostMapping("/transaction")
    public ResponseEntity<Object> saveTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        if (transactionService.failsLuhn(transactionDTO.getNumber()) || !isValidRegion(transactionDTO.getRegion())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(transactionService.saveTransaction(convertTransactionDTOToDomain(transactionDTO)));
    }

    @PutMapping("/transaction")
    public ResponseEntity<Object> updateTransaction(@Valid @RequestBody TransactionFeedbackDTO feedbackDTO) {
        Long id = feedbackDTO.getId();
        String feedback = feedbackDTO.getFeedback() != null ? feedbackDTO.getFeedback() : "";
        if (!transactionService.transactionExists(id)) {
            return ResponseEntity.notFound().build();
        } if (transactionService.feedbackForTransactionExists(id, feedback)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } if (!isValidFeedback(feedback)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(transactionService.updateTransactionFeedback(id, feedback));
    }

    @PostMapping("/suspicious-ip")
    public ResponseEntity<Object> saveSuspiciousIp(@Valid @RequestBody SuspiciousIpDTO ipDTO) {
        String ip = ipDTO.getIp();
        if (transactionService.flaggedAsSuspiciousIp(ip)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        var ipDomain = transactionService.saveSuspiciousIp(convertIpDTOToDomain(ipDTO));
        return ResponseEntity.ok().body(ipDomain);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<Object> deleteIp(@PathVariable String ip) {
        String ipRegex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        if (!ip.matches(ipRegex)) return ResponseEntity.badRequest().build();
        if (!transactionService.flaggedAsSuspiciousIp(ip)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().body(transactionService.deleteByIp(ip));
    }

    @GetMapping("/suspicious-ip")
    public ResponseEntity<Object> listIps() {
        return ResponseEntity.ok(transactionService.listSuspiciousIps());
    }

    @PostMapping("/stolencard")
    public ResponseEntity<Object> saveStolenCard(@Valid @RequestBody StolenCardDTO cardDTO) {
        String number = cardDTO.getNumber();
        if (transactionService.failsLuhn(number)) {
            return ResponseEntity.badRequest().build();
        } if (transactionService.flaggedAsStolenCard(number)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
            var cardDomain = transactionService.saveStolenCard(convertCardDTOToDomain(cardDTO));
            return ResponseEntity.ok().body(cardDomain);
        }
    }

    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<Object> deleteCard(@PathVariable String number) {
        if (transactionService.failsLuhn(number)) {
            return ResponseEntity.badRequest().build();
        } if (!transactionService.flaggedAsStolenCard(number)) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().body(transactionService.deleteByNumber(number));
        }
    }

    @GetMapping("/stolencard")
    public ResponseEntity<Object> listCards() {
        return ResponseEntity.ok(transactionService.listStolenCards());
    }

    @GetMapping("/history")
    public ResponseEntity<Object> listTransactionHistories() {
        return ResponseEntity.ok(transactionService.listTransactionHistories());
    }

    @GetMapping("/history/{number}")
    public ResponseEntity<Object> listTransactionHistory(@PathVariable String number) {
        if (transactionService.failsLuhn(number)) {
            return ResponseEntity.badRequest().build();
        }

        var transactionHistoryList = transactionService.listTransactionHistory(number);
        if (transactionHistoryList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(transactionHistoryList);
    }

    public boolean isValidFeedback(String feedback) {
        return EnumSet.allOf(TransactionStatus.class)
                .stream()
                .anyMatch(status -> status.name().equals(feedback));
    }

    public boolean isValidRegion(TransactionRegions inputRegion) {
        return EnumSet.allOf(TransactionRegions.class)
                .stream()
                .anyMatch(region -> region.equals(inputRegion));
    }

    public TransactionDomain convertTransactionDTOToDomain(TransactionDTO transactionDTO) {
        return controllerModelMapper.map(transactionDTO, TransactionDomain.class);
    }

    public SuspiciousIpDomain convertIpDTOToDomain(SuspiciousIpDTO ipDTO) {
        return controllerModelMapper.map(ipDTO, SuspiciousIpDomain.class);
    }

    public StolenCardDomain convertCardDTOToDomain(StolenCardDTO cardDTO) {
        return controllerModelMapper.map(cardDTO, StolenCardDomain.class);
    }
}