package antifraud.business.services;

import antifraud.business.models.cards.CardDomain;
import antifraud.business.models.cards.CardEntity;
import antifraud.business.models.suspicious.cards.StolenCardDomain;
import antifraud.business.models.suspicious.cards.StolenCardEntity;
import antifraud.business.models.suspicious.ips.SuspiciousIpDomain;
import antifraud.business.models.suspicious.ips.SuspiciousIpEntity;
import antifraud.business.models.transactions.TransactionDomain;
import antifraud.business.models.transactions.TransactionEntity;
import antifraud.business.models.transactions.TransactionStatus;
import antifraud.persistence.CardRepository;
import antifraud.persistence.StolenCardRepository;
import antifraud.persistence.SuspiciousIpRepository;
import antifraud.persistence.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TransactionService {

    @Autowired
    private final TransactionRepository transactionRepository;

    @Autowired
    private final StolenCardRepository stolenCardRepository;

    @Autowired
    private final SuspiciousIpRepository suspiciousIpRepository;

    @Autowired
    private final CardRepository cardRepository;

    @Autowired
    private final ModelMapper serviceModelMapper;

    public Map<String, String> saveTransaction(TransactionDomain transactionDomain) {
        Map<String, String> statusMap = new ConcurrentHashMap<>(2);
        var number = transactionDomain.getNumber();
        var cardDomain = convertTransactionDomainToCardDomain(transactionDomain);
        if (!cardRepository.existsByNumber(number)) {
            cardRepository.save(convertCardDomainToEntity(cardDomain));
        }

        var cardEntity = cardRepository.findByNumber(number);
        var cardAllowedLimit = cardEntity.getAllowedLimit();
        var cardManualProcessingLimit = cardEntity.getManualProcessingLimit();
        long amount = transactionDomain.getAmount();
        transactionDomain.setResult(amount <= cardAllowedLimit ? TransactionStatus.ALLOWED
                        : amount <= cardManualProcessingLimit ? TransactionStatus.MANUAL_PROCESSING
                        : TransactionStatus.PROHIBITED);


        List<String> infoList = infoList(transactionDomain, amount, number);

        transactionRepository.save(convertTransactionDomainToEntity(transactionDomain));

        String info;
        if (infoList.isEmpty()) {
            info = "none";
        } else {
            Collections.sort(infoList);
            info = String.join(", ", infoList);
        }
        statusMap.put("result", transactionDomain.getResult().toString());
        statusMap.put("info", info);
        return statusMap;
    }

    @Transactional
    public TransactionDomain updateTransactionFeedback(Long id, String feedback) {
        var potentialTransaction = transactionRepository.findById(id);
        if (potentialTransaction.isPresent()) {
            var transaction = potentialTransaction.get();
            if (!transaction.getFeedback().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }

            transaction.setFeedback(feedback);

            var validity = transaction.getResult().toString();
            var amount = transaction.getAmount();
            var cardEntity = cardRepository.findByNumber(transaction.getNumber());
            feedbackSystem(cardEntity, amount, validity, feedback);
            return convertTransactionEntityToDomain(transaction);
        }
        return null;
    }

    public SuspiciousIpDomain saveSuspiciousIp(SuspiciousIpDomain ipDomain) {
        var entity = convertIpDomainToEntity(ipDomain);
        suspiciousIpRepository.save(entity);
        return convertIpEntityToDomain(entity);
    }

    @Transactional
    public Map<String, String> deleteByIp(String ip) {
        suspiciousIpRepository.deleteByIp(ip);
        Map<String, String> deleteMsg = new ConcurrentHashMap<>(1);
        deleteMsg.put("status", "IP " + ip + " successfully removed!");
        return deleteMsg;
    }

    public List<SuspiciousIpDomain> listSuspiciousIps() {
        return suspiciousIpRepository.findByOrderById().stream()
                .map(this::convertIpEntityToDomain)
                .toList();
    }

    public StolenCardDomain saveStolenCard(StolenCardDomain cardDomain) {
        var entity = convertCardDomainToEntity(cardDomain);
        stolenCardRepository.save(entity);
        return convertCardEntityToDomain(entity);
    }

    @Transactional
    public Map<String, String> deleteByNumber(String number) {
        stolenCardRepository.deleteByNumber(number);
        Map<String, String> deleteMsg = new ConcurrentHashMap<>(1);
        deleteMsg.put("status", "Card " + number + " successfully removed!");
        return deleteMsg;
    }

    public List<StolenCardDomain> listStolenCards() {
        return stolenCardRepository.findByOrderById().stream()
                .map(this::convertCardEntityToDomain)
                .toList();
    }

    public List<TransactionDomain> listTransactionHistories() {
        return transactionRepository.findByOrderById().stream()
                .map(this::convertTransactionEntityToDomain)
                .toList();
    }

    public List<TransactionDomain> listTransactionHistory(String number) {
        return transactionRepository.findByNumberOrderById(number).stream()
                .map(this::convertTransactionEntityToDomain)
                .toList();
    }

    // Utility Methods
    public List<String> infoList(TransactionDomain transactionDomain, Long amount, String number) {
        List<String> infoList = new ArrayList<>(5);
        if (transactionDomain.getResult() != TransactionStatus.ALLOWED) {
            infoList.add("amount");
        }

        var ip = transactionDomain.getIp();
        if (flaggedAsSuspiciousIp(transactionDomain.getIp())) {
            transactionDomain.setResult(TransactionStatus.PROHIBITED);
            infoList.add("ip");
        }

        if (flaggedAsStolenCard(number)) {
            transactionDomain.setResult(TransactionStatus.PROHIBITED);
            infoList.add("card-number");
        }

        var region = transactionDomain.getRegion();
        var date = transactionDomain.getDate();
        var lastHour = date.minusHours(1);

        var regionCount = transactionRepository.countUniqueRegionsInLastHour(region, number, lastHour, date);
        if (regionCount >= 2) {
            if (regionCount == 2) {
                transactionDomain.setResult(TransactionStatus.MANUAL_PROCESSING);
            } else {
                transactionDomain.setResult(TransactionStatus.PROHIBITED);
            }
            infoList.add("region-correlation");
        }

        var ipCount = transactionRepository.countUniqueIpsInLastHour(ip, number, lastHour, date);
        if (ipCount >= 2) {
            if (ipCount == 2) {
                transactionDomain.setResult(TransactionStatus.MANUAL_PROCESSING);
            } else {
                transactionDomain.setResult(TransactionStatus.PROHIBITED);
            }
            infoList.add("ip-correlation");
        }

        // If transaction was slated for MANUAL_PROCESSING but then flagged as suspicious:
        // Remove "amount" as a cause
        if ((amount > 200 && amount <= 1500) && infoList.size() > 1) {
            infoList.remove(0);
        }

        return infoList;
    }

    public void feedbackSystem(CardEntity cardEntity, Long amount, String validity, String feedback) {
        var allowedLimit = cardEntity.getAllowedLimit();
        var manualLimit = cardEntity.getManualProcessingLimit();
        switch (feedback) {
            case "ALLOWED" -> {
                switch (validity) {
                    case "ALLOWED"           -> throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
                    case "MANUAL_PROCESSING" -> cardEntity.setAllowedLimit(increaseLimit(allowedLimit, amount));
                    case "PROHIBITED"        -> {
                        cardEntity.setAllowedLimit(increaseLimit(allowedLimit, amount));
                        cardEntity.setManualProcessingLimit(increaseLimit(manualLimit, amount));
                    }
                }
            } case "MANUAL_PROCESSING" -> {
                switch (validity) {
                    case "ALLOWED"           -> cardEntity.setAllowedLimit(decreaseLimit(allowedLimit, amount));
                    case "MANUAL_PROCESSING" -> throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
                    case "PROHIBITED"        -> cardEntity.setManualProcessingLimit(increaseLimit(manualLimit, amount));
                }
            } case "PROHIBITED" -> {
                switch (validity) {
                    case "ALLOWED"           -> {
                        cardEntity.setAllowedLimit(decreaseLimit(allowedLimit, amount));
                        cardEntity.setManualProcessingLimit(decreaseLimit(manualLimit, amount));
                    }
                    case "MANUAL_PROCESSING" -> cardEntity.setManualProcessingLimit(decreaseLimit(manualLimit, amount));
                    case "PROHIBITED"        -> throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }
        }
    }

    // Luhn's algorithm for a String input, acquired from GeeksForGeeks
    // Returns true if given card number is valid
    public boolean failsLuhn(String number) {
        int nDigits = number.length();
        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--) {
            int d = number.charAt(i) - '0';
            if (isSecond) {
                d = d * 2;
            }
            // We add two digits to handle cases that make two digits after doubling
            nSum += d / 10;
            nSum += d % 10;
            isSecond = !isSecond;
        }
        return (nSum % 10 != 0);
    }

    public boolean feedbackForTransactionExists(Long id, String feedback) {
        return transactionRepository.existsByIdAndFeedback(id, feedback);
    }

    public boolean transactionExists(Long id) {
        return transactionRepository.existsById(id);
    }

    public boolean flaggedAsSuspiciousIp(String ip) {
        return suspiciousIpRepository.existsByIp(ip);
    }

    public boolean flaggedAsStolenCard(String number) {
        return stolenCardRepository.existsByNumber(number);
    }

    public Long increaseLimit(Long limit, Long amount) {
        return (long) Math.ceil((0.8 * limit) + (0.2 * amount));
    }

    public Long decreaseLimit(Long limit, Long amount) {
        return (long) Math.ceil((0.8 * limit) - (0.2 * amount));
    }

    public TransactionDomain convertTransactionEntityToDomain(TransactionEntity transactionEntity) {
        return serviceModelMapper.map(transactionEntity, TransactionDomain.class);
    }

    public TransactionEntity convertTransactionDomainToEntity(TransactionDomain transactionDomain) {
        return serviceModelMapper.map(transactionDomain, TransactionEntity.class);
    }

    public SuspiciousIpDomain convertIpEntityToDomain(SuspiciousIpEntity ipEntity) {
        return serviceModelMapper.map(ipEntity, SuspiciousIpDomain.class);
    }
    public SuspiciousIpEntity convertIpDomainToEntity(SuspiciousIpDomain ipDomain) {
        return serviceModelMapper.map(ipDomain, SuspiciousIpEntity.class);
    }

    public StolenCardDomain convertCardEntityToDomain(StolenCardEntity cardEntity) {
        return serviceModelMapper.map(cardEntity, StolenCardDomain.class);
    }
    public StolenCardEntity convertCardDomainToEntity(StolenCardDomain cardDomain) {
        return serviceModelMapper.map(cardDomain, StolenCardEntity.class);
    }

    public CardDomain convertTransactionDomainToCardDomain(TransactionDomain transactionDomain) {
        return serviceModelMapper.map(transactionDomain, CardDomain.class);
    }

    public CardEntity convertCardDomainToEntity(CardDomain cardDomain) {
        return serviceModelMapper.map(cardDomain, CardEntity.class);
    }
}