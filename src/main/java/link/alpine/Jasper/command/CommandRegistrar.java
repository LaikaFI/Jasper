package link.alpine.Jasper.command;

import com.google.common.reflect.ClassPath;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static link.alpine.Jasper.util.LoggingManager.error;
import static link.alpine.Jasper.util.LoggingManager.info;
import static link.alpine.Jasper.util.LoggingManager.debug;

/**
 * CommandRegistrar Class
 * Used for easy command package loading, we use this to avoid having 20 lines of list.add() methods.
 * @author Laika
 */
public class CommandRegistrar {

    /**
     * Locates all classes that contain the package name provided. Use CAREFULLY.
     * @param packageName - the name of the package to look for
     * @return - A set of classes that contain that package name.
     */
    public Set<Class> findAllClassesContaining(String packageName) {
        try {
            return ClassPath.from(ClassLoader.getSystemClassLoader())
                    .getAllClasses()
                    .stream()
                    .filter(clazz -> clazz.getPackageName()
                            .contains(packageName))
                    .map(clazz -> clazz.load())
                    .collect(Collectors.toSet());
        } catch (Exception ex) {
            error("Failed to load classes containing " + packageName + ", check stack.");
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Utilizes findAllClassesContaining() to find all command classes and return them in a simple manner.
     * @return - The CommandClass's located.
     */
    public List<CommandClass> getCommandClasses() {
        try {
            // TODO have this check the classpath that we're under and have it scan *that* instead of hard-coding it to only be moe.oko.Kiafumi path.
            var classes = findAllClassesContaining("moe.oko.Kiafumi.command");
            List<CommandClass> commands = new ArrayList<>();
            debug("Discovered " + classes.size() + " classes containing moe.oko.Kiafumi.command in package class.");
            for (Class clazz : classes) {
                for (Constructor cnstr : clazz.getConstructors()) {
                    try {
                        var obj = cnstr.newInstance(); // making an attempt.
                        if (obj instanceof CommandClass) {
                            debug("Loading command class %green(" + cnstr.getName() + ").");
                            commands.add((CommandClass) obj);
                        }
                    } catch (InstantiationException ex) {
                        // Ignore, this is just us trying to load the CommandClass abstract class.
                    }
                }
            }
            info("Loaded [" + commands.size() + "] command classes.");
            return commands;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            // Now we don't ignore, this is a core issue.
            exception.printStackTrace();
            error("Failure in command class loading.");
            return null;
        }
    }
}
