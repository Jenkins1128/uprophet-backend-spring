package com.uprophet.quote;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uprophet.auth.security.AuthUser;
import com.uprophet.quote.dto.AddCommentRequest;
import com.uprophet.quote.dto.CommentResponse;
import com.uprophet.quote.dto.CreateQuoteRequest;
import com.uprophet.quote.dto.HomeQuoteResponse;
import com.uprophet.quote.dto.QuoteIdRequest;

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

    @PostMapping("/like")
    public ResponseEntity<Void> like(@Valid @RequestBody QuoteIdRequest request, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        quoteService.like(user.id(), user.username(), request.quoteId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unlike")
    public ResponseEntity<Void> unlike(@Valid @RequestBody QuoteIdRequest request, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        quoteService.unlike(user.id(), request.quoteId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/quotes/{quoteId}")
    public HomeQuoteResponse quote(@PathVariable long quoteId, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return quoteService.quote(user.id(), quoteId);
    }

    @GetMapping("/quotes/{quoteId}/comments")
    public List<CommentResponse> comments(@PathVariable long quoteId) {
        return quoteService.comments(quoteId);
    }

    @PostMapping("/addComment")
    public CommentResponse addComment(@Valid @RequestBody AddCommentRequest request, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return quoteService.addComment(user.username(), request.quoteId(), request.comment());
    }

    @DeleteMapping("/deleteQuote")
    public ResponseEntity<Void> delete(@Valid @RequestBody QuoteIdRequest request) {
        quoteService.delete(request.quoteId());
        return ResponseEntity.ok().build();
    }
}
