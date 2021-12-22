package com.example.mshare.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mshare.databinding.UsersViewContainerBinding;
import com.example.mshare.listener.UserListener;
import com.example.mshare.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private UserListener userListener;

    public UserAdapter(List<User> userList, UserListener userListener) {
        this.userList = userList;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UsersViewContainerBinding usersViewContainerBinding = UsersViewContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false);
        return new UserViewHolder(usersViewContainerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        UsersViewContainerBinding binding;

        public UserViewHolder(UsersViewContainerBinding itemView) {
            super(itemView.getRoot());

        }
    }
}
