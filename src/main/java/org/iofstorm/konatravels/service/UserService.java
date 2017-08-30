package org.iofstorm.konatravels.service;

import org.iofstorm.konatravels.Utils;
import org.iofstorm.konatravels.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.iofstorm.konatravels.Utils.SC_BAD_REQUEST;
import static org.iofstorm.konatravels.Utils.SC_NOT_FOUND;
import static org.iofstorm.konatravels.Utils.SC_OK;

public class UserService {
    public static final Map<Integer, User> users = new HashMap<>(1_041_000, 1f);
    private final ReadWriteLock lock;

    private VisitService visitService;

    public UserService() {
        lock = new ReentrantReadWriteLock(true);
    }

    public void setVisitService(VisitService visitService) {
        this.visitService = visitService;
    }

    public User getUserWithoutLock(Integer id) {
        return users.get(id);
    }

    public int createUser(User user) {
        lock.readLock().lock();
        try {
            if (users.containsKey(user.getId())) return SC_BAD_REQUEST;
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (users.containsKey(user.getId())) {
                    return SC_BAD_REQUEST;
                } else {
                    users.put(user.getId(), user);
                    return SC_OK;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @SuppressWarnings("NumberEquality")
    public int updateUser(Integer userId, User userUpdate) {
        lock.readLock().lock();
        try {
            if (!users.containsKey(userId)) return SC_NOT_FOUND;

            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!users.containsKey(userId)) return SC_NOT_FOUND;
                if (userUpdate == null) return SC_BAD_REQUEST;

                remapUser(users.get(userId), userUpdate);
                return SC_OK;
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    // used for data loading
    public void load(List<User> userList) {
        for (User usr : userList) {
            usr.setAge(Utils.calcAge(usr.getBirthDate()));
            users.put(usr.getId(), usr);
        }
    }

    private User remapUser(User oldUser, User newUser) {
        if (newUser.getEmail() != null) oldUser.setEmail(newUser.getEmail());
        if (newUser.getFirstName() != null) oldUser.setFirstName(newUser.getFirstName());
        if (newUser.getLastName() != null) oldUser.setLastName(newUser.getLastName());
        if (newUser.getBirthDate() != Long.MIN_VALUE) {
            oldUser.setBirthDate(newUser.getBirthDate());
            oldUser.setAge(newUser.getAge());
        }
        if (newUser.getGender() != null) oldUser.setGender(newUser.getGender());

        return oldUser;
    }
}
