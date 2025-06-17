package editor.action;

public interface Command {
    void execute();
    void undo();
}