package cms.blog.controller;

import cms.blog.dto.Permission;
import cms.blog.dto.Post;
import cms.blog.service.AuthorizationException;
import cms.blog.service.ServiceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

@Controller
public class AdminController {

     @Autowired
     private ServiceLayer service;

     @Inject
     public AdminController(ServiceLayer service) {
        this.service = service;
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String displayApprovedPosts(Model model) {

        Permission permission = Permission.ADMIN;
        List<Post> posts = service.getPosts(permission);
        model.addAttribute("blogs", posts);

        return "admin/admin";
    }

    @RequestMapping(value = "/admin/pending", method = RequestMethod.GET)
    public String displayPendingPosts(Model model) {
        Permission permission = Permission.ADMIN;
        try {
            List<Post> posts = service.getNotApprovedPosts(permission);
            model.addAttribute("blogs", posts);
        }
        catch (AuthorizationException ex) {
            return "/";
            }
        return "admin/admin";
    }

    @GetMapping("/admin/editPost/{postId}")
    public String showEditPost(@PathVariable("postId") int postId,
                            Model model){
        Post post = service.getPostById(postId);
        model.addAttribute("post", post);
        return "admin/adminEdit";
    }

    @GetMapping("/admin/{postId}")
    public String showPost(@PathVariable("postId") int postId,
                            Model model){
        Post post = service.getPostById(postId);
        model.addAttribute("post", post);
        return "admin/adminPost";
    }

    @PostMapping("/admin/editPost/adminEdit")
    public String editPost(HttpServletRequest request,
                           Model model){
         Post post = new Post();
         post.setPostId(Integer.parseInt(request.getParameter("postId")));
         post.setTitle(request.getParameter("title"));
         post.setDescription(request.getParameter("description"));
         post.setPostContent(request.getParameter("content"));
         if (request.getParameter("publishDate") != null) {
             post.setPublishDate(LocalDate.parse(request.getParameter("publishDate")));
         }
         if (request.getParameter("expireDate") != null) {
             post.setExpireDate(LocalDate.parse(request.getParameter("expireDate")));
         }
        try {
            service.editPost(post, Permission.ADMIN);
        } catch (AuthorizationException ex) {
        }
        return "redirect:/admin/";
    }

    @GetMapping("/admin/deletePost/{postId}")
    public String deletePost(@PathVariable("postId") int postId) {
        //int id = Integer.parseInt(request.getParameter("postId"));
        try {
            service.deletePost(postId, Permission.ADMIN);
        } catch (AuthorizationException ex) {
        }

        return "redirect:/admin/";
    }

    @PostMapping("/admin/approve")
    public String approvePost(HttpServletRequest request){
        int postId = Integer.parseInt(request.getParameter("postId"));
        try {
            service.approvePost(postId, Permission.ADMIN);
        } catch (AuthorizationException ex) {
        }
        return "redirect:/admin/";
    }

    @PostMapping("/admin/editPost/reject")
    public String rejectPost(HttpServletRequest request){
        int postId = Integer.parseInt(request.getParameter("postId"));
        String reason = request.getParameter("reason");
        try {
            service.rejectPost(postId, reason, Permission.ADMIN);
        } catch (AuthorizationException ex) {
        }
        return "redirect:/admin/";
    }

    @GetMapping("admin/create")
    public String showCreate() {
         return "/admin/create";
    }

    @PostMapping("/adminCreate")
    public String createPost(HttpServletRequest request,
                           Model model){
         Post post = new Post();
         post.setTitle(request.getParameter("title"));
         post.setDescription(request.getParameter("description"));
         post.setPostContent(request.getParameter("content"));
         if (request.getParameter("publishDate") != null) {
             post.setPublishDate(LocalDate.parse(request.getParameter("publishDate")));
         }
         if (request.getParameter("expireDate") != null) {
             post.setExpireDate(LocalDate.parse(request.getParameter("expireDate")));
         }
        try {
            service.addPost(post, Permission.ADMIN);
        } catch (AuthorizationException ex) {
        }
        return "redirect:/admin/";
    }
}
