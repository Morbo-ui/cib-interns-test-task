package ru.morboui.raiff.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.morboui.raiff.entity.Socks;
import ru.morboui.raiff.enums.Operations;
import ru.morboui.raiff.exceptions.IncorrectParametersException;
import ru.morboui.raiff.exceptions.InvalidResultException;
import ru.morboui.raiff.repository.SocksRepository;


@Service
public class SocksService {

    private final SocksRepository socksRepository;

    @Autowired
    public SocksService(SocksRepository socksRepository) {
        this.socksRepository = socksRepository;
    }

    public void addNewSocks(@NonNull Socks socks) {
        if (!(socks.getQuantity() != 0 && socks.getCottonPart() > 0 && socks.getCottonPart() <= 100)) {
            throw new IncorrectParametersException("Quantity should be greater than 0 and CottonPart should be from 0 to 100");
        } else if (socksRepository.findSocksByColorAndCottonPartEquals(socks.getColor(), socks.getCottonPart()).isEmpty())
            socksRepository.save(socks);
        else {
            Socks socksInBD = socksRepository.findSocksByColorAndCottonPartEquals(socks.getColor(), socks.getCottonPart()).get();
            socksInBD.setQuantity(socks.getQuantity() + socksInBD.getQuantity());
            socksRepository.save(socksInBD);
        }
//
    }


    public void reduceSocks(@NonNull Socks socks) {
        if (socksRepository.findSocksByColorAndCottonPartEquals(socks.getColor(), socks.getCottonPart()).isPresent()) {
            Socks socksInBD = socksRepository.findSocksByColorAndCottonPartEquals(socks.getColor(), socks.getCottonPart()).get();
            socksInBD.setQuantity(Math.max(socksInBD.getQuantity() - socks.getQuantity(), -1));

            if (socksInBD.getQuantity() == -1) {
                throw new IncorrectParametersException("Not enough socks for the outcome");
            }
            socksRepository.save(socksInBD);
        } else throw new IncorrectParametersException("There's no socks with such color or CottonPart");
    }

    public @NonNull Socks getByColorAndCottonPart(@NonNull String color, @NonNull Operations operation, @NonNull Long cottonPart) {
        switch (operation) {
            case moreThan:
                return socksRepository.getSocksByColorAndCottonPartIsGreaterThan(color, cottonPart).orElseThrow(() ->
                        new IncorrectParametersException("Nothing was found for this parameters"));
            case lessThan:
                return socksRepository.getSocksByColorAndCottonPartIsLessThan(color, cottonPart).orElseThrow(() ->
                        new IncorrectParametersException("Nothing was found for this parameters"));
            case equals:
                return socksRepository.findSocksByColorAndCottonPartEquals(color, cottonPart).orElseThrow(() ->
                        new IncorrectParametersException("Nothing was found for this parameters"));
            default:
                throw new InvalidResultException("Incorrect parameters in URL");
        }
    }
}
