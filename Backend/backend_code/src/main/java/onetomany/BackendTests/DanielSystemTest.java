package onetomany.BackendTests;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import onetomany.Group.Group;
import onetomany.Group.GroupController;
import onetomany.Group.GroupRepository;
import onetomany.Users.User;
import onetomany.Users.UserRepository;
import onetomany.userLogIn.userLogin;
import onetomany.userLogIn.userLoginController;
import onetomany.userLogIn.userLoginRepository;
import onetomany.AdminActivityReport.adminActivityReportRepository;
import onetomany.Reports.ReportsRepository;
import onetomany.Sellers.SellerRepository;
import onetomany.adminUser.adminUserRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {GroupController.class, userLoginController.class})
public class DanielSystemTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private GroupRepository groupRepository;
    @MockBean private UserRepository userRepository;

    @MockBean private userLoginRepository userLoginRepository;
    @MockBean private SellerRepository sellerRepository;
    @MockBean private ReportsRepository reportsRepository;
    @MockBean private adminUserRepository addminUserRepository;
    @MockBean private adminActivityReportRepository adddminActivityReportRepository;

    @Test
    public void testCreateGroupAndListMembership() throws Exception {
        Group created = new Group("study");
        created.setId(5);

        when(groupRepository.findByName("study")).thenReturn(null).thenReturn(created);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> {
            Group g = invocation.getArgument(0);
            if (g.getId() == 0) {
                g.setId(5);
            }
            return g;
        });

        mockMvc.perform(post("/groups/create/study"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        User member = new User();
        member.setUsername("daniel");
        created.setUsers(Set.of(member));

        when(userRepository.findByUsername("daniel")).thenReturn(member);
        when(groupRepository.findAll()).thenReturn(List.of(created));

        mockMvc.perform(get("/groups/list/daniel").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("study")));
    }

    @Test
    public void testAddUserToGroup() throws Exception {
        Group group = new Group("alpha");
        group.setId(3);

        User user = new User();
        user.setUsername("alex");

        when(groupRepository.findById(3)).thenReturn(group);
        when(userRepository.findByUsername("alex")).thenReturn(user);
        when(groupRepository.save(group)).thenReturn(group);
        when(userRepository.save(user)).thenReturn(user);

        mockMvc.perform(post("/groups/group/add-user/3/alex"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("User added to group successfully")));

        verify(groupRepository).save(group);
        verify(userRepository).save(user);
    }

    @Test
    public void testAddUserToGroupWhenNotFound() throws Exception {
        when(groupRepository.findById(8)).thenReturn(null);
        when(userRepository.findByUsername("ghost")).thenReturn(null);

        mockMvc.perform(post("/groups/group/add-user/8/ghost"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Group or user not found")));
    }

    @Test
    public void testUserLoginEndpoints() throws Exception {
        userLogin login = new userLogin();
        login.setEmail("test@example.com");
        login.setType('u');
        login.setPassword("secret");
        login.setUserName("tester");

        when(userLoginRepository.findByEmail("test@example.com")).thenReturn(login);
        when(userLoginRepository.findByUserName("tester")).thenReturn(login);

        mockMvc.perform(get("/userslogin/getUserType/test@example.com").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("u")));

        mockMvc.perform(get("/userslogin/tester/wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid password")));
    }

    @Test
    public void testCreateGroupDuplicateAndMembers() throws Exception {
        Group original = new Group("gamers");
        original.setId(2);

        when(groupRepository.findByName("gamers")).thenReturn(original);

        mockMvc.perform(post("/groups/create/gamers"))
                .andExpect(status().isOk())
                .andExpect(content().string("-1"));

        User member = new User();
        member.setUsername("lin");
        original.setUsers(Set.of(member));

        when(groupRepository.findById(2)).thenReturn(original);

        mockMvc.perform(get("/groups/getMembers/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("lin")));
    }
}
