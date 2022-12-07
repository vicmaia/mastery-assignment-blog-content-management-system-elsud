/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cms.blog.controller;

import cms.blog.dao.PostDao;
import cms.blog.dao.HashtagDao;
import cms.blog.dto.Permission;
import static cms.blog.dto.Permission.ADMIN;
import static cms.blog.dto.Permission.USER;

import cms.blog.dto.Post;
import cms.blog.service.ServiceLayer;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import javax.inject.Inject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author victoriamaia
 */

@Controller
public class BlogController {
    
    @Autowired
    private ServiceLayer service;
    
    @Autowired
    private PostDao postDao;
    
    @Inject
    public BlogController(ServiceLayer service) {
        this.service = service;
    }
    
    /*
   @GetMapping("/home")
    public String displayApprovedPosts(Model model) {
        List<Post> posts = postDao.getApprovedPostsForAdmin();
        model.addAttribute("posts", posts);
        
        return "blog/view_posts";
    }
    */
    
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String displayMainPage(Model model) {
        
        Permission permission = USER;
        List<Post> postResponse = service.getPosts(permission);
        model.addAttribute("postResponse", postResponse);
        
        return "blog/view_posts";
    }
    
    
    

    @GetMapping("/post/{postId}")
    public String showPost(@PathVariable("postId") int postId,
                            Model model){
        Post post = service.getPostById(postId);
        model.addAttribute("post", post);
        return "blog/blog_post";
    }

    
    
    
    
    // handler method to handle blog post search request
    // http://localhost:8080/page/search?query=java
    @RequestMapping("/page/search")
    public String searchPosts(@RequestParam(value = "query") String query,
                              Model model){
        Permission permission = USER;
        List<Post> postResponse = service.getPostsByContent(query, permission);
        model.addAttribute("postResponse", postResponse);
        return "blog/view_posts";
    }
    
    
    @RequestMapping("/login")
    public String login(Model model){
        return "login";
    }
    
    /*
    @RequestMapping("/")
    public String home(Model model){
        return "admin/posts";
    }
*/
    
    /*
    @RequestMapping("newpost")
    public String createPost(Model model){
        return "admin/create_post";
    }
*/

}
