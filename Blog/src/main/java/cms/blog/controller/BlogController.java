/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cms.blog.controller;

import cms.blog.dao.PostDao;
import cms.blog.dao.HashtagDao;
import cms.blog.dto.HashTag;
import cms.blog.dto.Permission;

import cms.blog.dto.Post;
import cms.blog.dto.RejectedPost;
import cms.blog.service.AuthorizationException;
import cms.blog.service.ServiceLayer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.jdbc.core.JdbcTemplate;

import static cms.blog.dto.Permission.*;

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

    @PostMapping("/addTag")
    public String addTag(HttpServletRequest request, Model model) {
        String name = request.getParameter("tag");
        int postId = Integer.parseInt(request.getParameter("postId"));
        service.addTagForPost(name, postId);
        Post post = service.getPostById(postId);
        model.addAttribute("post", post);
        return "redirect:/";
    }

    @GetMapping("/searchByTag/{tagId}")
    public String searchByTag(@PathVariable("tagId") int tagId,
                            Model model){
        List<Post> posts = service.getPostsByTag(tagId, USER);
        model.addAttribute("postResponse", posts);
        return "blog/view_posts";
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

//    MANAGER FUNCTIONS
@RequestMapping(value = "/manager/pending", method = RequestMethod.GET)
public String displayPendingPost(Model model) throws AuthorizationException {
    Permission permission = MANAGER;
    List<Post> pending = service.getNotApprovedPosts(permission);
    model.addAttribute("posts", pending);

    return "manager/pending";
}

    @RequestMapping(value = "/manager/rejected", method = RequestMethod.GET)
    public String displayRejectedPost(Model model) throws AuthorizationException {
        Permission permission = MANAGER;
        List<RejectedPost> rejected = service.getRejectedPosts(permission);
        model.addAttribute("posts", rejected);

        return "manager/rejected";
    }


    @PostMapping("/manager/create")
    public String addPost(HttpServletRequest request) throws AuthorizationException {
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String shortDescription = request.getParameter("shortDescription");
        String expDate = request.getParameter("expireDate");
        String pubDate = request.getParameter("publishDate");

        Post post = new Post();
        post.setTitle(title);
        post.setPostContent(content);
        post.setDescription(shortDescription);
        if (pubDate != null && pubDate != "") {
            post.setPublishDate(LocalDate.parse(pubDate));
        }
        if (expDate  != null && expDate != "" ) {
            post.setExpireDate(LocalDate.parse(expDate ));
        }
        //post.setPublishDate(service.parseDateInput(pubDate));
        //post.setExpireDate(service.parseDateInput(expDate));
        service.addPost(post, MANAGER);

        return "redirect:/manager/pending";
    }

    @GetMapping("/manager/create")
    public String displayCreationPage(Model model) {
        return "manager/create";
    }

    @RequestMapping(value = "manager/deletePost/{postId}", method = RequestMethod.GET)
    public String deletePost(@PathVariable Integer postId) throws AuthorizationException {
            Post post = service.getPostById(postId);
        service.deletePost(postId, MANAGER);

        return "redirect:/manager/pending";
    }


    @RequestMapping(value ="/manager/editPost/{postId}", method = RequestMethod.GET)
    public String editPost(@PathVariable Integer postId, Model model) {
        Post post = postDao.getPostById(postId);
        model.addAttribute("post", post);
        return "manager/editPost";
    }
    
    @RequestMapping(value ="/manager/editPost/rejected/{postId}", method = RequestMethod.GET)
    public String editRejectedPost(@PathVariable Integer postId, Model model) {
        Post post = postDao.getRejectedPostById(postId);
        model.addAttribute("post", post);
        return "manager/editRejectedPost";
    }

    @RequestMapping(value = "/manager/editPost/{postId}", method = RequestMethod.POST)
    public String updatePost(@PathVariable Integer postId, HttpServletRequest request, Model model) throws AuthorizationException {
        Post currentPost = service.getPostById(postId);

        currentPost.setTitle(request.getParameter("title"));
        currentPost.setDescription(request.getParameter("shortDescription"));
        currentPost.setPostContent(request.getParameter("content"));
        if (request.getParameter("publishDate") != null && request.getParameter("publishDate") != "") {
            currentPost.setPublishDate(LocalDate.parse(request.getParameter("publishDate")));
        }
        if (request.getParameter("expireDate") != null && request.getParameter("expireDate") != "") {
            currentPost.setExpireDate(LocalDate.parse(request.getParameter("expireDate")));
        }
        //currentPost.setPublishDate(request.getParameter("publishDate"));
        //currentPost.setExpireDate(request.getParameter("expireDate"));

        service.editPost(currentPost, MANAGER);

        model.addAttribute("post", currentPost);

        return "redirect:/manager/pending";
    }
    
    @RequestMapping(value = "/manager/editPost/rejected/{postId}", method = RequestMethod.POST)
    public String updateRejectedPost(@PathVariable Integer postId, HttpServletRequest request, Model model) throws AuthorizationException {
        Post currentPost = service.getPostById(postId);

        currentPost.setTitle(request.getParameter("title"));
        currentPost.setDescription(request.getParameter("shortDescription"));
        currentPost.setPostContent(request.getParameter("content"));
        if (request.getParameter("publishDate") != null && request.getParameter("publishDate") != "") {
            currentPost.setPublishDate(LocalDate.parse(request.getParameter("publishDate")));
        }
        if (request.getParameter("expireDate") != null && request.getParameter("expireDate") != "") {
            currentPost.setExpireDate(LocalDate.parse(request.getParameter("expireDate")));
        }
        //currentPost.setPublishDate(request.getParameter("publishDate"));
        //currentPost.setExpireDate(request.getParameter("expireDate"));

        service.editPost(currentPost, MANAGER);

        model.addAttribute("post", currentPost);

        return "redirect:/manager/rejected";
    }
    
    @RequestMapping(value = "/manager/rejected/resubmit/{postId}", method = {RequestMethod.POST, RequestMethod.GET})
    public String resubmit(@PathVariable Integer postId, Model model) throws AuthorizationException {
        service.sendToApprove(postId, MANAGER);

        return "redirect:/manager/rejected";
    }


}
