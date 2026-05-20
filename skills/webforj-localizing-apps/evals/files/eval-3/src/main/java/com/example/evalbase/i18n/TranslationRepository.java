package com.example.evalbase.i18n;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslationRepository extends JpaRepository<Translation, Long> {

  Optional<Translation> findByKeyAndLocale(String key, String locale);
}
