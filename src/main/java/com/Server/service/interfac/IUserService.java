package com.Server.service.interfac;

import com.Server.dto.LoginRequest;
import com.Server.dto.Response;
import com.Server.entity.User;

public interface IUserService {
    Response register(User user);

    Response login(LoginRequest loginRequest);

    Response getAllUsers(int page, int limit, String sort, String order);

    Response getUserBookingHistory(String userId);

    Response deleteUser(String userId);

    Response getUserById(String userId);

    Response getMyInfo(String email);
}
