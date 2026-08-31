package com.eldren.wutao.ramesses_curse.dynamictext.compat;

import com.eldren.wutao.ramesses_curse.dynamictext.text.DynamicTextParser;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 不硬链接 FTB 类的兼容接口。未安装 FTB Quests 时所有修改方法安全返回 false。
 * 任务数据应在服务端脚本加载阶段修改；客户端收到同步后会自动解析动态代码。
 */
public final class FtbQuestTextApi {
    private static final String API_CLASS = "dev.ftb.mods.ftbquests.api.FTBQuestsAPI";

    private FtbQuestTextApi() {
    }

    public static boolean isAvailable() {
        try {
            Class.forName(API_CLASS, false, FtbQuestTextApi.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static Component parse(String text) {
        return DynamicTextParser.parse(text);
    }

    public static boolean setTitle(long objectId, String text) {
        Object object = findObject(objectId);
        return object != null
                && invokeVoid(object, "setRawTitle", new Class<?>[]{String.class}, text)
                && clearObject(object);
    }

    /** 字符串 ID 按 FTB 的十六进制格式解析，避免脚本数值精度损失。 */
    public static boolean setTitle(String objectId, String text) {
        Long parsedId = parseObjectId(objectId);
        return parsedId != null && setTitle(parsedId, text);
    }

    public static boolean setSubtitle(long questId, String text) {
        Object quest = findObject(questId);
        return quest != null
                && invokeVoid(quest, "setRawSubtitle", new Class<?>[]{String.class}, text)
                && clearObject(quest);
    }

    public static boolean setSubtitle(String questId, String text) {
        Long parsedId = parseObjectId(questId);
        return parsedId != null && setSubtitle(parsedId, text);
    }

    @SuppressWarnings("unchecked")
    public static boolean setDescription(long questId, List<String> lines) {
        Object quest = findObject(questId);
        if (quest == null || lines == null) {
            return false;
        }
        try {
            Method getter = quest.getClass().getMethod("getRawDescription");
            Object value = getter.invoke(quest);
            if (!(value instanceof List<?> rawList)) {
                return false;
            }
            List<Object> mutable = (List<Object>) rawList;
            mutable.clear();
            mutable.addAll(lines);
            return clearObject(quest);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    public static boolean setDescription(long questId, String... lines) {
        return lines != null && setDescription(questId, List.of(lines));
    }

    public static boolean setDescription(String questId, List<String> lines) {
        Long parsedId = parseObjectId(questId);
        return parsedId != null && setDescription(parsedId, lines);
    }

    public static boolean setDescription(String questId, String... lines) {
        Long parsedId = parseObjectId(questId);
        return parsedId != null && lines != null && setDescription(parsedId, List.of(lines));
    }

    public static boolean refresh() {
        boolean refreshed = false;
        for (boolean serverSide : new boolean[]{true, false}) {
            Object file = getQuestFile(serverSide);
            if (file == null) {
                continue;
            }
            boolean cleared = invokeVoid(file, "clearCachedData", new Class<?>[0]);
            boolean gui = invokeVoid(file, "refreshGui", new Class<?>[0]);
            refreshed |= cleared || gui;
        }
        return refreshed;
    }

    private static Long parseObjectId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("0x") || normalized.startsWith("0X")) {
            normalized = normalized.substring(2);
        }
        try {
            return Long.parseUnsignedLong(normalized, 16);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Object findObject(long objectId) {
        for (boolean serverSide : new boolean[]{true, false}) {
            Object file = getQuestFile(serverSide);
            if (file == null) {
                continue;
            }
            try {
                Method getter = file.getClass().getMethod("getBase", long.class);
                Object object = getter.invoke(file, objectId);
                if (object != null) {
                    return object;
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // 尝试另一侧的任务文件。
            }
        }
        return null;
    }

    private static Object getQuestFile(boolean serverSide) {
        try {
            Class<?> apiClass = Class.forName(API_CLASS);
            Object api = apiClass.getMethod("api").invoke(null);
            return api.getClass().getMethod("getQuestFile", boolean.class).invoke(api, serverSide);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static boolean clearObject(Object object) {
        boolean cleared = invokeVoid(object, "clearCachedData", new Class<?>[0]);
        Object file = invoke(object, "getQuestFile", new Class<?>[0]);
        if (file != null) {
            invokeVoid(file, "markDirty", new Class<?>[0]);
            invokeVoid(file, "refreshGui", new Class<?>[0]);
        }
        return cleared;
    }

    private static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... arguments) {
        try {
            return target.getClass().getMethod(name, parameterTypes).invoke(target, arguments);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static boolean invokeVoid(Object target, String name, Class<?>[] parameterTypes, Object... arguments) {
        try {
            target.getClass().getMethod(name, parameterTypes).invoke(target, arguments);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
}
