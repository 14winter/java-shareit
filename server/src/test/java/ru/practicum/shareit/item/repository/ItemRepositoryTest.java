package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    private final int from = 0;
    private final int size = 10;
    private final PageRequest pageRequest = PageRequest.of(from / size, size);

    @Test
    public void findAllByOwner() {
        User user = User.builder().name("TestUser").email("test@email.com").build();
        entityManager.persist(user);
        User user1 = User.builder().name("TestUser1").email("test1@email.com").build();
        entityManager.persist(user1);
        Item item1 = Item.builder().name("TestItem1").description("description1").owner(user).build();
        entityManager.persist(item1);
        Item item2 = Item.builder().name("TestItem2").description("description2").owner(user).build();
        entityManager.persist(item2);
        Item item3 = Item.builder().name("TestItem3").description("description3").owner(user1).build();
        entityManager.persist(item3);
        entityManager.flush();
        List<Item> items = itemRepository.findAllByOwner(user, pageRequest);

        assertThat(items).hasSize(2);
        assertThat(items).contains(item1, item2);
        assertThat(items.get(0).getId()).isEqualTo(item1.getId());
        assertThat(items.get(0).getName()).isEqualTo(item1.getName());
        assertThat(items.get(0).getDescription()).isEqualTo(item1.getDescription());
        assertThat(items.get(1).getId()).isEqualTo(item2.getId());
        assertThat(items.get(1).getName()).isEqualTo(item2.getName());
        assertThat(items.get(1).getDescription()).isEqualTo(item2.getDescription());
    }

    @Test
    public void search() {
        String text = "Дрель";
        Item item1 = Item.builder().name("Дрель").description("Эллектрическая дрель").available(true).build();
        entityManager.persist(item1);
        Item item2 = Item.builder().name("Ручная Дрель").description("Ручная дрель").available(true).build();
        entityManager.persist(item2);
        Item item3 = Item.builder().name("Ручная Дрель").description("Ручная дрель").available(false).build();
        entityManager.persist(item3);
        entityManager.flush();
        List<Item> items = itemRepository.search(text, pageRequest);

        assertThat(items).hasSize(2);
        assertThat(items).contains(item1, item2);
        assertThat(items.get(0).getId()).isEqualTo(item1.getId());
        assertThat(items.get(0).getName()).isEqualTo(item1.getName());
        assertThat(items.get(0).getDescription()).isEqualTo(item1.getDescription());
        assertThat(items.get(1).getId()).isEqualTo(item2.getId());
        assertThat(items.get(1).getName()).isEqualTo(item2.getName());
        assertThat(items.get(1).getDescription()).isEqualTo(item2.getDescription());
    }

    @Test
    public void findAllByRequest_id() {
        ItemRequest itemRequest = ItemRequest.builder().description("description").build();
        entityManager.persist(itemRequest);
        ItemRequest itemRequest1 = ItemRequest.builder().description("description1").build();
        entityManager.persist(itemRequest1);
        Item item1 = Item.builder().name("TestItem1").description("description1").request(itemRequest).build();
        entityManager.persist(item1);
        Item item2 = Item.builder().name("TestItem2").description("description2").request(itemRequest).build();
        entityManager.persist(item2);
        Item item3 = Item.builder().name("TestItem3").description("description3").request(itemRequest1).build();
        entityManager.persist(item3);
        entityManager.flush();
        List<Item> items = itemRepository.findAllByRequest_id(itemRequest.getId());

        assertThat(items).hasSize(2);
        assertThat(items).contains(item1, item2);
        assertThat(items.get(0).getId()).isEqualTo(item1.getId());
        assertThat(items.get(0).getName()).isEqualTo(item1.getName());
        assertThat(items.get(0).getDescription()).isEqualTo(item1.getDescription());
        assertThat(items.get(1).getId()).isEqualTo(item2.getId());
        assertThat(items.get(1).getName()).isEqualTo(item2.getName());
        assertThat(items.get(1).getDescription()).isEqualTo(item2.getDescription());
    }

    @Test
    public void findAllByRequest_IdIn() {
        User user = User.builder().name("TestUser").email("test@email.com").build();
        entityManager.persist(user);
        ItemRequest itemRequest = ItemRequest.builder().description("description").requestor(user).build();
        entityManager.persist(itemRequest);
        ItemRequest itemRequest1 = ItemRequest.builder().description("description1").build();
        entityManager.persist(itemRequest1);
        Item item1 = Item.builder().name("TestItem1").description("description1").request(itemRequest).build();
        entityManager.persist(item1);
        Item item2 = Item.builder().name("TestItem2").description("description2").request(itemRequest).build();
        entityManager.persist(item2);
        Item item3 = Item.builder().name("TestItem3").description("description3").request(itemRequest1).build();
        entityManager.persist(item3);
        entityManager.flush();

        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(user.getId());

        List<Item> items = itemRepository.findAllByRequest_IdIn(itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList()));

        assertThat(items).hasSize(2);
        assertThat(items).contains(item1, item2);
        assertThat(items.get(0).getId()).isEqualTo(item1.getId());
        assertThat(items.get(0).getName()).isEqualTo(item1.getName());
        assertThat(items.get(0).getDescription()).isEqualTo(item1.getDescription());
        assertThat(items.get(1).getId()).isEqualTo(item2.getId());
        assertThat(items.get(1).getName()).isEqualTo(item2.getName());
        assertThat(items.get(1).getDescription()).isEqualTo(item2.getDescription());
    }
}
