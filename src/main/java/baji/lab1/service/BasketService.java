package baji.lab1.service;

import baji.lab1.entity.Basket;
import baji.lab1.repository.BasketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BasketService {

    @Autowired
    private BasketRepository basketRepository;

    public BasketService() {

    }

    public Basket findById(Long id) {
        return basketRepository.findById(id).orElse(null);
    }

    public Optional<Basket> findByUserId(Long id) {
        return basketRepository.findByUserId(id);
    }

    public Basket save(Basket basket) {
        return basketRepository.save(basket);
    }

    public void deleteById(Long id) {
        basketRepository.deleteById(id);
    }
}