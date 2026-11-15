package com.uprophet.quote;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uprophet.auth.security.AuthUser;
import com.uprophet.quote.dto.CreateQuoteRequest;
import com.uprophet.quote.dto.HomeQuoteResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping({"", "/"})
    public List<HomeQuoteResponse> home(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return quoteService.home(user.id(), user.username());
    }

    @GetMapping("/explore")
    public List<HomeQuoteResponse> explore(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return quoteService.explore(user.id());
    }

    @PostMapping("/createQuote")
    public HomeQuoteResponse create(@Valid @RequestBody CreateQuoteRequest request, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return quoteService.create(user.username(), request.title(), request.quote());
    }
}
