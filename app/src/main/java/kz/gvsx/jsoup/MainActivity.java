package kz.gvsx.jsoup;

import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

class RVAdapter extends RecyclerView.Adapter<RVAdapter.ReleaseViewHolder> {

    List<Element> notifications;

    RVAdapter(List<Element> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ReleaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_item, viewGroup, false);
        return new ReleaseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReleaseViewHolder releaseViewHolder, int i) {
        Element notification = notifications.get(i);
        Spanned notificationText = HtmlCompat.fromHtml(notification.html(), HtmlCompat.FROM_HTML_MODE_LEGACY);
        releaseViewHolder.textView.setText(notificationText);
        releaseViewHolder.textView.setMovementMethod(BetterLinkMovementMethod.getInstance());
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class ReleaseViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ReleaseViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

}

public class MainActivity extends AppCompatActivity {

    private static final String URL = "https://tou.edu.kz/ru/component/notifications";
    private final List<Element> notifications = new ArrayList<>();
    private RVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new RVAdapter(notifications);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                getNotifications();

                adapter.notifyDataSetChanged();
                runOnUiThread(() -> {
                    LinearLayoutManager llm = new LinearLayoutManager(this.getApplicationContext());
                    RecyclerView rv = findViewById(R.id.recyclerView);
                    rv.setLayoutManager(llm);
                    rv.setAdapter(adapter);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void getNotifications() throws IOException {
        Document doc = Jsoup.connect(URL).timeout(5000).get();
        Element notificationsDiv = doc.select("div.notification").first();

        for (Element notification : notificationsDiv.children()) {
            Element text = notification.select("div.introtext").first();

            // Convert relative paths into absolute.
            for (Element url : text.select("a[href]")) {
                url.attr("href", url.absUrl("href"));
            }

            notifications.add(text);
        }
    }
}