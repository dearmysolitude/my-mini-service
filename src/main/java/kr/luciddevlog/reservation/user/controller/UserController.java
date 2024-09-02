package kr.luciddevlog.reservation.user.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kr.luciddevlog.reservation.user.dto.LoginForm;
import kr.luciddevlog.reservation.user.dto.MemberInfo;
import kr.luciddevlog.reservation.user.dto.RegisterForm;
import kr.luciddevlog.reservation.user.entity.UserItem;
import kr.luciddevlog.reservation.user.exception.InvalidCredentialsException;
import kr.luciddevlog.reservation.user.exception.UserAlreadyExistsException;
import kr.luciddevlog.reservation.user.exception.UserNotFoundException;
import kr.luciddevlog.reservation.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String toLoginPage() {
        return "member/login";
    }

    @PostMapping("/login")
    public String login(@Valid LoginForm loginForm, BindingResult bindingResult, Model model, HttpSession session) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            model.addAttribute("error", errors);
            return "member/login";
        }

        if(session.getAttribute("loggedInMember") != null) {
            return "redirect:/board/list";
        }

        try {
            UserItem member = userService.login(loginForm);
            session.setAttribute("loggedInMember", new MemberInfo(member));
            return "redirect:/";
        } catch (UserNotFoundException | InvalidCredentialsException e) {
            model.addAttribute("error", e.getMessage());
            return "member/login";
        } catch (Exception e) {
            model.addAttribute("error", "로그인 중 예기치 않은 오류가 발생했습니다." + e.getMessage());
            return "member/login";
        }
    }

    @GetMapping("/signup")
    public String signup() {
        return "member/signup";
    }

    @PostMapping("/signup")
    public String makeNewUser(@Valid RegisterForm form, BindingResult bindingResult, Model model){
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            model.addAttribute("error", errors);
            return "member/signup";
        }

        List<String> message = new ArrayList<>();
        try {
            userService.register(form);
            message.add("회원 가입 완료: 로그인 하세요.");
            model.addAttribute("message", message);
            return "member/login";
        } catch (UserAlreadyExistsException e) {
            message.add(e.getMessage());
            model.addAttribute("message", message);
            return "member/signup";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}
