import { InputPanel } from './InputPanel'
import { TasksPanel } from './TasksPanel'

export function WorkspaceView({
  selectedProject,
  inputCollapsed,
  onToggleCollapse,
  inputText,
  onInputChange,
  language,
  onLanguageChange,
  taskDepth,
  onTaskDepthChange,
  onGenerate,
  genStatus,
  elapsed,
  progress,
  dialog,
  onSendAnswer,
  aiThinking,
  onEditBrief,
  generatedTasks,
  deletedTaskIds,
  onDeleteTask,
  onRestoreTask,
  onUpdateTask,
  confirmed,
  onConfirm,
  genProjectName,
}) {
  const projectTasks =
    selectedProject && selectedProject !== 'new'
      ? selectedProject.tasks ?? []
      : []

  return (
    <div className={`workspace${inputCollapsed ? ' workspace--collapsed' : ''}`}>
      <InputPanel
        collapsed={inputCollapsed}
        onToggleCollapse={onToggleCollapse}
        project={selectedProject}
        inputText={inputText}
        onInputChange={onInputChange}
        language={language}
        onLanguageChange={onLanguageChange}
        taskDepth={taskDepth}
        onTaskDepthChange={onTaskDepthChange}
        onGenerate={onGenerate}
        genStatus={genStatus}
        elapsed={elapsed}
        progress={progress}
        dialog={dialog}
        onSendAnswer={onSendAnswer}
        aiThinking={aiThinking}
        onEditBrief={onEditBrief}
      />

      <TasksPanel
        genStatus={genStatus}
        generatedTasks={generatedTasks}
        deletedTaskIds={deletedTaskIds}
        onDeleteTask={onDeleteTask}
        onRestoreTask={onRestoreTask}
        onUpdateTask={onUpdateTask}
        confirmed={confirmed}
        onConfirm={onConfirm}
        genProjectName={genProjectName}
        projectTasks={projectTasks}
      />
    </div>
  )
}
