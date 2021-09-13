package rmit.ad.rmitrides;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserDapterTutor extends RecyclerView.Adapter<UserDapterTutor.ViewHolder> {
    Context mContext;
    List<User> mUsers;

    public UserDapterTutor(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new UserDapterTutor.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.userName.setText(user.getUsername());
        if(user.getAvatar().equals("default")){
            holder.avatar.setImageResource(R.drawable.profileimage);
        }else{
            Glide.with(mContext).load(user.getAvatar()).into(holder.avatar);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MyMessageFragment.class);
                intent.putExtra("userId",user.getId());
                intent.putExtra("username",user.getUsername());
                mContext.startActivity(intent);
            }
        });

    }



    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView avatar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.username);
            avatar = itemView.findViewById(R.id.profile_image);
        }
    }
}
