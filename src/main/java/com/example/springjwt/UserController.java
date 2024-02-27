package com.example.springjwt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTUtil jwtUtil;


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(User user, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser != null && user.getPassword().equals(existingUser.getPassword())) {
            String token = jwtUtil.generateToken(existingUser.getEmail());
            HttpSession session = request.getSession();
            session.setAttribute("token", token);
            session.setAttribute("user", existingUser);
            if (existingUser.getType().equals("user")) {
                return "redirect:/user-detail";
            } else if (existingUser.getType().equals("admin")) {
                return "redirect:/admin-detail";
            }
        }
        redirectAttributes.addFlashAttribute("logerror", "Invalid email or password");
        return "redirect:/login";
    }



    @GetMapping("/user-detail")
    public String userDetail(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String token = (String) session.getAttribute("token");
        User user = (User) session.getAttribute("user");
        if (token != null && jwtUtil.validateToken(token) && user != null) {
            model.addAttribute("user", user);
            return "user-detail";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/admin-detail")
    public String adminDetail(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String token = (String) session.getAttribute("token");
        User user = (User) session.getAttribute("user");
        if (token != null && jwtUtil.validateToken(token)) {
            if (user != null && user.getType().equals("admin")) {
                List<User> users = userRepository.findAll();
                model.addAttribute("users", users);
                model.addAttribute("user", user);
                return "admin-detail";
            } else {
                model.addAttribute("error", "Sorry, You can't access this page.");
                return "user-detail";
            }
        } else {
            return "redirect:/login";
        }
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

