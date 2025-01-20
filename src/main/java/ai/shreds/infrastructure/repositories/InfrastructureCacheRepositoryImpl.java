package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.domain.ports.DomainPortCache;
import ai.shreds.domain.value_objects.DomainValueFlightSearchCriteria;
import ai.shreds.infrastructure.exceptions.InfrastructureDatabaseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

// Rest of the existing code remains the same...