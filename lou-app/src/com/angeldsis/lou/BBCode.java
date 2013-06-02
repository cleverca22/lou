package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;

import com.angeldsis.lou.reports.ShowReport;

public class BBCode {
	static class CityCordSpan extends ClickableSpan {
		int x,y;
		SessionUser2 c;
		public CityCordSpan(SessionUser2 context,int x, int y) {
			this.x = x;
			this.y = y;
			this.c = context;
		}
		@Override
		public void onClick(View widget) {
			Log.v(TAG,"link clicked");
			Bundle args = c.getAcct().toBundle();
			args.putInt("x",x);
			args.putInt("y",y);
			Intent i = new Intent(c.getActivity(),ShowCoord.class);
			i.putExtras(args);
			c.getActivity().startActivity(i);
		}
	}
	static private final String TAG = "BBCode";
	static Pattern purl = Pattern.compile("^(.*?)\\[url\\](.*?)\\[/url\\](.*)$",Pattern.DOTALL);
	static Pattern preport = Pattern.compile("^(.*)([A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4})(.*)$",Pattern.DOTALL);
	static Pattern pbold = Pattern.compile("^(.*?)\\[b\\](.+?)\\[/b\\](.*)$", Pattern.DOTALL);
	static Pattern pcity = Pattern.compile("^(.*)\\[(city|coords)\\](\\d+):(\\d+)\\[/(city|coords)\\](.*)$",Pattern.DOTALL);
	public static void parse(SessionUser2 context,String bbcode,SpannableStringBuilder builder,ArrayList<Span> spans) {
		Matcher m = purl.matcher(bbcode);
		if (m.find()) {
			String a = m.group(1);
			String b = m.group(2);
			String c = m.group(3);
			BBCode.parse(context, a, builder, spans);
			int start = builder.length();
			builder.append(b);
			int end = builder.length();
			spans.add(new Span(new LinkClicked(context,b), start, end));
			BBCode.parse(context, c, builder, spans);
			return;
		}
		m = preport.matcher(bbcode);
		if (m.find()) {
			String a = m.group(1);
			String b = m.group(2);
			String c = m.group(3);
			BBCode.parse(context, a, builder, spans);
			int start = builder.length();
			builder.append(b);
			int end = builder.length();
			spans.add(new Span(new ReportClicked(context,b), start, end));
			BBCode.parse(context, c, builder, spans);
			return;
		}
		m = pbold.matcher(bbcode);
		if (m.find()) {
			String a = m.group(1);
			String b = m.group(2);
			String c = m.group(3);
			BBCode.parse(context, a, builder, spans);
			int start = builder.length();
			builder.append(b);
			int end = builder.length();
			spans.add(new Span(new StyleSpan(Typeface.BOLD),start,end));
			BBCode.parse(context, c, builder, spans);
			return;
		}
		m = pcity.matcher(bbcode);
		// [coord]...[/coord] normally opens the map
		// [city]...[/city] normally opens the city view via GetPublicCityInfo
		if (m.find()) {
			String a = m.group(1);
			String b = m.group(3);
			String c = m.group(4);
			String d = m.group(6);
			BBCode.parse(context, a, builder, spans);
			int x = Integer.parseInt(b);
			int y = Integer.parseInt(c);
			
			int start = builder.length();
			builder.append(b);
			builder.append(":");
			builder.append(c);
			int end = builder.length();
			BBCode.parse(context, d, builder, spans);
			spans.add(new Span(new CityCordSpan(context,x,y),start,end));
			return;
		}
		builder.append(bbcode);
	}
	static class LinkClicked extends ClickableSpan {
		String url;
		SessionUser2 context;
		public LinkClicked(SessionUser2 context2, String b) {
			url = b;
			this.context = context2;
		}
		@Override
		public void onClick(View widget) {
			Intent openLink = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
			context.getActivity().startActivity(openLink);
		}
	}
	static class ReportClicked extends ClickableSpan {
		String shareid;
		private SessionUser2 context;
		ReportClicked(SessionUser2 context2, String r) {
			shareid = r;
			this.context = context2;
		}
		@Override public void onClick(View v) {
			Log.v(TAG,"report clicked "+shareid);
			Bundle args = context.getAcct().toBundle();
			args.putString("shareid",shareid);
			Intent i = new Intent(context.getActivity(),ShowReport.class);
			i.putExtras(args);
			context.getActivity().startActivity(i);
		}
	}
	public static class Span {
		Object o;
		int start,end;
		public Span(Object linkClicked, int start, int end) {
			o = linkClicked;
			this.start = start;
			this.end = end;
		}
		public void apply(SpannableStringBuilder b) {
			b.setSpan(o, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
}
