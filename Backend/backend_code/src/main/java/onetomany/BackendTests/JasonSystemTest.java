package onetomany.BackendTests;

import onetomany.Items.Item;
import onetomany.Items.ItemsController;
import onetomany.Items.ItemsRepository;
import onetomany.Items.ItemImageRepository;
import onetomany.Sellers.SellerRepository;
import onetomany.Users.User;
import onetomany.Users.UserController;
import onetomany.Users.UserRepository;
import onetomany.Users.PasswordRecoveryService;
import onetomany.Users.UserImageRepository;
import onetomany.userLogIn.userLoginRepository;
import onetomany.userLogIn.userLogin;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {UserController.class, ItemsController.class})
public class JasonSystemTest {

    @Autowired
    private MockMvc controller;


    @MockBean private UserRepository userRepository;
    @MockBean private userLoginRepository userLoginRepo;
    @MockBean private PasswordRecoveryService passwordRecoveryService;
    @MockBean private UserImageRepository userImageRepository;
    @MockBean private ItemsRepository itemsRepository;
    @MockBean private SellerRepository sellerRepository;
    @MockBean private ItemImageRepository itemImageRepository;

    @Test
    public void testCreateAndFetchUser() throws Exception {
        List<User> userList = new ArrayList<>();

        when(userRepository.save(any(User.class))).thenAnswer(x -> {
            User r = x.getArgument(0);
            if(r.getId() == 0) r.setId(1);
            userList.add(r);
            return r;
        });

        when(userLoginRepo.save(any(userLogin.class))).thenReturn(null);

        when(userRepository.findUserByUsername(any(String.class))).thenAnswer(x -> {
             String name = x.getArgument(0);
             for (User u : userList) {
                 if (u.getUsername() != null && u.getUsername().equals(name)) return u;
             }
             return null;
        });

        when(userRepository.findById(anyInt())).thenAnswer(x -> {
            int id = x.getArgument(0);
            for (User u : userList) {
                if (u.getId() == id) return u;
            }
            return null;
        });

        String jsonUser = "{\"username\":\"testUser\", \"emailId\":\"test@example.com\", \"userPassword\":\"password123\"}";

        controller.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("success")));

        controller.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testUser")));
    }

    @Test
    public void testCreateItem() throws Exception {
        List<Item> itemList = new ArrayList<>();

        when(itemsRepository.save(any(Item.class))).thenAnswer(x -> {
            Item i = x.getArgument(0);
            if(i.getId() == 0) i.setId(100);
            itemList.add(i);
            return i;
        });
        
        when(itemsRepository.findAll()).thenReturn(itemList);

        String jsonItem = "{\"name\":\"Gaming Console\", \"description\":\"New\", \"price\":499.99}";

        controller.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonItem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("success")));

        System.out.println("Items count: " + itemList.size());
    }
    


    @Test
    public void testDeleteUser() throws Exception {
        List<User> userList = new ArrayList<>();
        User u = new User();
        u.setId(1);
        u.setUsername("deleteMe");
        userList.add(u);

        when(userRepository.findById(1)).thenReturn(u);

        doAnswer(x -> {
            userList.remove(u);
            return null;
        }).when(userRepository).delete(u);

        controller.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("success")));
                
        if (userList.isEmpty()) {
            System.out.println("User deleted successfully in mock.");
        }
    }
}
