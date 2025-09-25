package ru.yandex.practicum.catsgram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.*;

// Указываем, что класс PostService - является бином и его
// нужно добавить в контекст приложения
@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService;

    @Autowired
    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Collection<Post> findAll(Optional<String> sort, Optional<Integer> size, Optional<Integer> from) {
        List<Post> postList = new ArrayList<>(posts.values());
        if (sort.isPresent() && size.isPresent() && from.isPresent() && size.get() > 0) {
            if (from.get() + 1 <= postList.size() - 1) {
                if (size.get() <= postList.size() &&
                        size.get() <= ((postList.size() - 1 - from.get() + 1) + size.get() - 1)) {
                    return sortPostList(SortOrder.from(sort.get()),
                            postList.subList(from.get() + 1, (from.get() + 1 + size.get())));
                } else {
                    return sortPostList(SortOrder.from(sort.get()),
                            postList.subList(from.get() + 1, postList.size()));
                }
            }
            throw new ConditionsNotMetException("From превышает допустимое количество постов");

        } else if (sort.isPresent() && size.isPresent() && size.get() > 0) {
            if (size.get() >= postList.size()) {
                return sortPostList(SortOrder.from(sort.get()), postList);
            }

            return sortPostList(SortOrder.from(sort.get()), postList.subList(0, size.get()));
        }

        return postList;
    }

    private Collection<Post> sortPostList(SortOrder sortOrder, Collection<Post> postCollection) {
        if (sortOrder.equals(SortOrder.ASCENDING)) {
            return postCollection.stream()
                    .sorted(Comparator.comparing(Post::getPostDate))
                    .toList();
        }
        return postCollection.stream()
                .sorted(Comparator.comparing(Post::getPostDate).reversed())
                .toList();

    }

    public Post create(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }

        if (userService.getUserById(post.getAuthorId()).isEmpty()) {
            throw new ConditionsNotMetException("Автор с id = " + post.getAuthorId() + " не найден»");
        }

        post.setId(getNextId());
        post.setPostDate(Instant.now());

        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    public Optional<Post> getPostById(@RequestBody Long id) {
        return Optional.ofNullable(posts.get(id));
    }


    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}