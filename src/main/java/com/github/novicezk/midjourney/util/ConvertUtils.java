package com.github.novicezk.midjourney.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.TaskAction;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import lombok.experimental.UtilityClass;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ConvertUtils {
	/**
	 * content正则匹配prompt和进度.
	 */
	public static final String CONTENT_REGEX = ".*?\\*\\*(.*?)\\*\\*.+<@\\d+> \\((.*?)\\)";

	public static ContentParseData parseContent(String content) {
		return parseContent(content, CONTENT_REGEX);
	}

	public static ContentParseData parseContent(String content, String regex) {
		if (CharSequenceUtil.isBlank(content)) {
			return null;
		}
		Matcher matcher = Pattern.compile(regex).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		ContentParseData parseData = new ContentParseData();
		parseData.setPrompt(matcher.group(1));
		parseData.setStatus(matcher.group(2));
		return parseData;
	}

	public static TaskAction convertCustomId2Action(TaskAction targetAction, String customId) {
		if (customId.contains("upsample")) {
			return TaskAction.UPSCALE;
		}
		if (customId.contains("variation") || customId.contains("Inpaint")) {
			return TaskAction.VARIATION;
		}
		if (customId.contains("Outpaint") || customId.contains("CustomZoom")) {
			return TaskAction.ZOOM;
		}
		if (customId.contains("pan")) {
			return TaskAction.PAN;
		}
		if (customId.contains("Picread::Retry")) {
			return TaskAction.DESCRIBE;
		}
		if (customId.contains("Job::PicReader::") || customId.contains("Job::PromptAnalyzer::")) {
			// describe, shorten 后选择生图
			return TaskAction.IMAGINE;
		}
		if (customId.contains("PromptAnalyzerExtended")) {
			return TaskAction.SHORTEN;
		}
		if (customId.contains("reroll")) {
			return targetAction;
		}
		return null;
	}

	public static List<DataUrl> convertBase64Array(List<String> base64Array) throws MalformedURLException {
		if (base64Array == null || base64Array.isEmpty()) {
			return Collections.emptyList();
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		List<DataUrl> dataUrlList = new ArrayList<>();
		for (String base64 : base64Array) {
			DataUrl dataUrl = serializer.unserialize(base64);
			dataUrlList.add(dataUrl);
		}
		return dataUrlList;
	}

	public static TaskChangeParams convertChangeParams(String content) {
		List<String> split = CharSequenceUtil.split(content, " ");
		if (split.size() != 2) {
			return null;
		}
		String action = split.get(1).toLowerCase();
		TaskChangeParams changeParams = new TaskChangeParams();
		changeParams.setId(split.get(0));
		if (action.charAt(0) == 'u') {
			changeParams.setAction(TaskAction.UPSCALE);
		} else if (action.charAt(0) == 'v') {
			changeParams.setAction(TaskAction.VARIATION);
		} else {
			return null;
		}
		try {
			int index = Integer.parseInt(action.substring(1, 2));
			if (index < 1 || index > 4) {
				return null;
			}
			changeParams.setIndex(index);
		} catch (Exception e) {
			return null;
		}
		return changeParams;
	}

}
