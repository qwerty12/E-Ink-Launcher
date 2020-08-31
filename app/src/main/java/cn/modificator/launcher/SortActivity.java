package cn.modificator.launcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.modificator.launcher.widgets.RatioImageView;

public class SortActivity extends Activity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sort);

    AutoFitRecyclerView recyclerView = findViewById(R.id.sort_recycler_view);
    recyclerView.setAdapter(new SortItemAdapter());
  }
}

class SortItemAdapter extends RecyclerView.Adapter<SortItemAdapter.SortItemHolder> {
  @NonNull
  @Override
  public SortItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new SortItemHolder(
            LayoutInflater.from(parent.getContext())
              .inflate(R.layout.sort_item, parent, false)
    );
  }

  @Override
  public void onBindViewHolder(@NonNull SortItemHolder holder, int position) {
    ImageView icon = holder.itemView.findViewById(R.id.sort_item_icon);
    TextView name = holder.itemView.findViewById(R.id.sort_item_name);
    EditText priorityInput = holder.itemView.findViewById(R.id.sort_item_priority_input);

    icon.setImageResource(R.mipmap.ic_launcher);
    name.setText("App1");
    priorityInput.setText("0");
  }

  @Override
  public int getItemCount() {
    return 100;
  }

  static class SortItemHolder extends RecyclerView.ViewHolder {
    public View itemView;
    public SortItemHolder(View v) {
      super(v);
      itemView = v;
    }
  }
}
