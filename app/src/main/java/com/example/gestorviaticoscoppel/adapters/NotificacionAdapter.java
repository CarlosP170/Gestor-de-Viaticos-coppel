package com.example.gestorviaticoscoppel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.gestorviaticoscoppel.R;
import com.example.gestorviaticoscoppel.models.Notificacion;
import java.util.List;

public class NotificacionAdapter extends BaseAdapter {

    private Context context;
    private List<Notificacion> notificaciones;
    private LayoutInflater inflater;
    private OnNotificacionClickListener listener;

    public interface OnNotificacionClickListener {
        void onNotificacionClick(Notificacion notificacion, int position);
    }

    public NotificacionAdapter(Context context, List<Notificacion> notificaciones) {
        this.context = context;
        this.notificaciones = notificaciones;
        this.inflater = LayoutInflater.from(context);
    }

    public void setOnNotificacionClickListener(OnNotificacionClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return notificaciones.size();
    }

    @Override
    public Notificacion getItem(int position) {
        return notificaciones.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_notificacion, parent, false);
            holder = new ViewHolder();
            holder.textTitulo = convertView.findViewById(R.id.textTituloNotificacion);
            holder.textMensaje = convertView.findViewById(R.id.textMensajeNotificacion);
            holder.textFecha = convertView.findViewById(R.id.textFechaNotificacion);
            holder.indicadorNoLeida = convertView.findViewById(R.id.indicadorNoLeida);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notificacion notificacion = getItem(position);

        holder.textTitulo.setText(notificacion.getTitulo());
        holder.textMensaje.setText(notificacion.getMensaje());
        holder.textFecha.setText(notificacion.getFechaFormateada());

        if (!notificacion.isLeida()) {
            holder.indicadorNoLeida.setVisibility(View.VISIBLE);
            convertView.setAlpha(1.0f);
        } else {
            holder.indicadorNoLeida.setVisibility(View.GONE);
            convertView.setAlpha(0.7f);
        }

        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificacionClick(notificacion, position);
            }
        });

        return convertView;
    }

    public void updateNotificaciones(List<Notificacion> nuevasNotificaciones) {
        this.notificaciones = nuevasNotificaciones;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView textTitulo;
        TextView textMensaje;
        TextView textFecha;
        View indicadorNoLeida;
    }
}