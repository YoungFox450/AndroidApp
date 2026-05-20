package com.example.nox_group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.PersonViewHolder> {

    private List<Person> personList;
    private final OnPersonClickListener listener;

    public interface OnPersonClickListener {
        void onPersonClick(Person person);
    }

    public PersonAdapter(List<Person> personList, OnPersonClickListener listener) {
        this.personList = personList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        Person person = personList.get(position);
        String displayName = holder.itemView.getContext().getString(R.string.short_name_format, person.prenom, person.nom);
        holder.btnPerson.setText(displayName);
        holder.btnPerson.setOnClickListener(v -> listener.onPersonClick(person));
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    public void updateData(List<Person> newList) {
        this.personList = newList;
        notifyDataSetChanged();
    }

    static class PersonViewHolder extends RecyclerView.ViewHolder {
        Button btnPerson;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            btnPerson = (Button) itemView;
        }
    }
}
