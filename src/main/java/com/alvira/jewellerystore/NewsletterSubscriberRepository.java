package com.alvira.jewellerystore;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    NewsletterSubscriber findByEmail(String email);
}