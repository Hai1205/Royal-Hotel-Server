package com.Server.service.interfac;

import com.Server.dto.LoginRequest;
import com.Server.dto.Response;
import com.Server.model.User;

public interface IUserService {

    Response register(User user);

    Response login(LoginRequest loginRequest);

    Response getAllUsers();

    Response getUSerBookingHistory(String userId);

    Response deleteUser(String userId);

    Response getUserById(String userId);

    Response getMyInfo(String email);
}
