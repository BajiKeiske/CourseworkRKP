package baji.lab1.controller;

import baji.lab1.entity.Bundle;
import baji.lab1.repository.BundleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BundleViewController {

    @Autowired
    private BundleRepository bundleRepository;

    @GetMapping("/bundles/{id}")
    public String viewBundle(@PathVariable Long id, Model model) {
        Bundle bundle = bundleRepository.findById(id).orElse(null);
        model.addAttribute("bundle", bundle);
        return "bundle_details";
    }
}