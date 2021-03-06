package com.mycompany.wheretogo.service;

import com.mycompany.wheretogo.model.Vote;
import com.mycompany.wheretogo.repository.RestaurantRepository;
import com.mycompany.wheretogo.repository.UserRepository;
import com.mycompany.wheretogo.repository.VoteRepository;
import com.mycompany.wheretogo.util.exception.NotFoundException;
import com.mycompany.wheretogo.util.exception.OutOfDateTimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class VoteServiceImpl implements VoteService {
    private static final LocalTime ALLOWED_UPDATE_TIME_THRESHOLD = LocalTime.of(11, 0);

    private final UserRepository userRepository;

    private final RestaurantRepository restaurantRepository;

    private final VoteRepository voteRepository;

    @Autowired
    public VoteServiceImpl(UserRepository userRepository, VoteRepository voteRepository, RestaurantRepository restaurantRepository) {
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    @Transactional
    public Vote add(Vote vote, Integer restaurantId, Integer userId) {
        Assert.notNull(vote, "vote must not be null");
        Assert.notNull(restaurantId, "restaurantId must not be null");
        Assert.notNull(userId, "userId must not be null");
        if (!vote.getDateTime().toLocalDate().equals(LocalDate.now())) {
            throw new OutOfDateTimeException("Operation is not allowed - only today's votes applicable");
        }
        return saveUserVote(vote, restaurantId, userId);
    }

    @Override
    @Transactional
    public void update(Vote vote, Integer restaurantId, Integer userId) throws OutOfDateTimeException, NotFoundException {
        Assert.notNull(vote, "vote must not be null");
        Assert.notNull(userId, "userId must not be null");
        if (vote.getDateTime().compareTo(LocalDateTime.of(LocalDate.now(), ALLOWED_UPDATE_TIME_THRESHOLD)) >= 0) {
            throw new OutOfDateTimeException("Operation is not allowed - it's too late to change the vote");
        }
        saveUserVote(vote, restaurantId, userId);
    }

    @Override
    public List<Vote> getAll(Integer userId) {
        Assert.notNull(userId, "userId must not be null");
        return voteRepository.findAll(userId);
    }

    @Override
    public List<Vote> getAllBetweenDates(LocalDate startDate, LocalDate endDate, Integer userId) {
        Assert.notNull(startDate, "startDate must not be null");
        Assert.notNull(endDate, "endDate must not be null");
        Assert.notNull(userId, "userId must not be null");
        return voteRepository.findAllBetweenDateTimes(LocalDateTime.of(startDate, LocalTime.MIN), LocalDateTime.of(endDate, LocalTime.MAX), userId);
    }

    private Vote getUserVote(Integer id, Integer userId) {
        return voteRepository.findById(id).filter(vote -> userId.equals(vote.getUser().getId())).orElse(null);
    }

    private Vote saveUserVote(Vote vote, Integer restaurantId, Integer userId) {
        if (!vote.isNew() && getUserVote(vote.getId(), userId) == null) {
            return null;
        }
        vote.setRestaurant(restaurantRepository.getOne(restaurantId));
        vote.setUser(userRepository.getOne(userId));
        return voteRepository.save(vote);
    }
}
