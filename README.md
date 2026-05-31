# Trof

> AI task generation for [Vikunja](https://vikunja.io/) — it intercepts bad tasks before they hit your board. Describe a project in plain language and let AI break it down into structured Vikunja tasks. It asks clarifying questions when needed, and **nothing is written to your tracker until you review and confirm every change.**

> **Why "Trof"?** Named after the [Trophy](https://en.wikipedia.org/wiki/Trophy_(countermeasure)) active protection system on the Merkava tank, which intercepts incoming threats before they reach the hull. Trof does the same for your task board: the AI proposes, but every task passes through your review before it ever lands in Vikunja so nothing gets in that you didn't approve.

### Homepage

![Homepage](docs/media/home_page.png)

## Features

- **Create and edit projects** — start a new project or modify an existing one.
- **AI-assisted breakdown** — the AI turns your description into a structured set of tasks.
- **Clarifying questions** — the AI can ask you follow-up questions to refine the decomposition.
- **Full control over changes** — edit task names, descriptions, comments, and tags, or delete generated tasks before saving.
- **Explicit confirmation** — nothing is written to Vikunja until you confirm.

## Configuration

1. Copy the example environment file:
   ```bash
   cp example.env .env
   ```

2. Set your Vikunja connection in `.env`:

   ```env
   VIKUNJA_URL=https://your.vikunja.instance/api/v1
   VIKUNJA_TOKEN=your-vikunja-token-here
   ```

3. Enable the AI provider you want to use. Uncomment its block in `.env` and fill in the API key.
   For example, to use Anthropic:

   ```env
   AI_KOOG.ANTHROPIC.ENABLED=true
   AI_KOOG.ANTHROPIC.API_KEY=sk-ant-api***AA
   AI_KOOG.ANTHROPIC.BASE_URL=https://api.anthropic.com

   AI_MODEL_ID="claude-sonnet-4-6"
   ```

   Supported providers: OpenAI, Anthropic, Google, OpenRouter, DeepSeek, and Ollama
   (Ollama runs locally and needs no API key).

4. Provide TLS certificates for nginx by placing `cert.crt` and `cert.key` in the `./data` directory.

5. Build and start the stack:
   ```bash
   ./gradlew build -x test && docker compose up -d
   ```

6. Done! Trof is now running on your machine at **https://localhost**.

## Example

Here's what a typical project-creation flow looks like.

**1. Describe what you want to build:**

![First prompt](docs/media/first_prompt.png)

**2. Answer the AI's clarifying questions:**

![Answering the questions](docs/media/answers_the_questions.png)

**3. Review and confirm the generated tasks:**

![Confirming the tasks](docs/media/confirm_the_tasks.png)

**4. See the result in Vikunja:**

![Viewing the project in Vikunja](docs/media/view_the_project.png)

Editing an existing project follows the same flow.