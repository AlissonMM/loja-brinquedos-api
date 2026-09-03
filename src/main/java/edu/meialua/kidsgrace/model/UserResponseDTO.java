package edu.meialua.kidsgrace.model;

import edu.meialua.kidsgrace.adapters.in.Role;
import edu.meialua.kidsgrace.adapters.in.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Representação de usuário para respostas da API — deliberadamente sem o campo
 * "password". Usar em qualquer endpoint que devolve dados de User para o
 * cliente, em vez de serializar a entidade diretamente.
 */
public class UserResponseDTO {

    private Long id;
    private String name;
    private String userName;
    private String email;
    private String telephone;
    private String address;
    private int imageProfile;
    private List<String> roles;

    public UserResponseDTO() {
    }

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.telephone = user.getTelephone();
        this.address = user.getAddress();
        this.imageProfile = user.getImageProfile();
        this.roles = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getImageProfile() {
        return imageProfile;
    }

    public void setImageProfile(int imageProfile) {
        this.imageProfile = imageProfile;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
