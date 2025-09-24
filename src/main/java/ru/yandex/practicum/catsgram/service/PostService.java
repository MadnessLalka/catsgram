package ru.yandex.practicum.catsgram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
        if (sort.isPresent() && size.isPresent() && from.isPresent()) {
            System.out.println("Выбрана сортировка с отбрасыванием постов ");
            List<Collection<Post>> postList = List.of(posts.values());

            switch (SortOrder.from(String.valueOf(sort))){
                case ASCENDING -> {
                    return postList.stream()
                            .sorted(Comparator.comparing(Post::getPostDate))
                            .collect(Collectors.toList());
                }
            }

        } else if (sort.isPresent() && size.isPresent() && from.isEmpty()) {
            System.out.println("Выбрана сортировка без отбрасывания постов ");
        }

        return posts.values();
    }

//    private List<Post> sortPostListByAscending(Collection<Post> postCollection){
//        Comparator<Instant> instantAscComparator  = Collections.sort(postCollection, (pos))
//        List<Post> sortedPostList = new ArrayList<>().stream()
//                .sorted(Instant)
//
//        for (Post post : postCollection){
//            sortedPostList.sort();
//        }
//    }


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