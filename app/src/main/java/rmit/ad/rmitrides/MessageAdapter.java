package rmit.ad.rmitrides;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    Context mContext;
    List<Chat> mChats;
    String imageURL;

    public MessageAdapter(Context mContext, List<Chat> Chats, String imageURL) {
        this.mContext = mContext;
        this.mChats = Chats;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT){
            view = LayoutInflater.from(mContext).inflate(R.layout.chatitem_right,parent,false);
        }else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chatitem_left,parent,false);
        }



        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChats.get(position);
        holder.message.setText(chat.message);
        if(imageURL.equals("default")){
            holder.avatar.setImageResource(R.drawable.profileimage);
        }else {
            Glide.with(mContext).load(imageURL).into(holder.avatar);
        }

    }



    @Override
    public int getItemCount() {
        return mChats.size();
    }

    @Override
    public int getItemViewType(int position) {
        String currentUserID = FireBaseRef.mAuth.getCurrentUser().getUid();
        if(mChats.get(position).getSender().equals(currentUserID)){
            return MSG_TYPE_RIGHT;
        }
        else
            return MSG_TYPE_LEFT;
//        return super.getItemViewType(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        ImageView avatar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.text_send);
            avatar = itemView.findViewById(R.id.profile_image);
        }
    }
}
