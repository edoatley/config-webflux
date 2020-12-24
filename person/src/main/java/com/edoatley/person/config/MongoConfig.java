package com.edoatley.person.config;

import com.edoatley.person.repository.PersonRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = PersonRepository.class)
public class MongoConfig {
}
