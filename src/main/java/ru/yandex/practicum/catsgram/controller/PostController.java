package ru.yandex.practicum.catsgram.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ParameterNotValidException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.service.PostService;
import ru.yandex.practicum.catsgram.service.SortOrder;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Collection<Post> findAll(
            @RequestParam(defaultValue = "desc") Optional<String> sort,
            @RequestParam(defaultValue = "10") Optional<Integer> size,
            @RequestParam Optional<Integer> from
    ) {
        if (SortOrder.from(String.valueOf(sort)) == null) {
            throw new ParameterNotValidException(String.valueOf(sort), "Неверное наименование параметра сортировки");
        }

        if (size.get() <= 0) {
            throw new ParameterNotValidException(size.get().toString(), "Size не может быть меньше нуля");
        }

        if (from.get() < 0) {
            throw new ParameterNotValidException(from.get().toString(), "From не может быть меньше нуля");
        }

        return postService.findAll(sort, size, from);
    }


    @GetMapping("{id}")
    public Optional<Post> getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@RequestBody Post post) {
        return postService.create(post);
    }

    @PutMapping
    public Post update(@RequestBody Post newPost) {
        return postService.update(newPost);
    }
}
