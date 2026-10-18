import json
import logging
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

from dotenv import load_dotenv
from openai import OpenAI
from pydantic import BaseModel, Field, ValidationError

load_dotenv()

logging.basicConfig(level=os.getenv("LOG_LEVEL", "INFO"))
logger = logging.getLogger(__name__)

OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://ollama:11434/v1")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "llama3.2")
AI_ENGINE_PORT = int(os.getenv("AI_ENGINE_PORT", "8000"))


class SimulationRequest(BaseModel):
    prompt: str = Field(min_length=1)


class SimulationResponse(BaseModel):
    response: str
    source: str


def rule_based_response(prompt: str) -> str:
    """Return a deterministic response when the local model is unavailable."""
    return (
        "The simulation is operating in rule-based mode. "
        f"Based on the current event ({prompt}), prioritize cash flow, "
        "maintain a reserve, and review the market before expanding."
    )


def generate_response(prompt: str) -> SimulationResponse:
    try:
        client = OpenAI(
            api_key=os.getenv("OLLAMA_API_KEY", "ollama"),
            base_url=OLLAMA_BASE_URL,
        )
        completion = client.chat.completions.create(
            model=OLLAMA_MODEL,
            messages=[
                {
                    "role": "system",
                    "content": "You are an advisor for a capitalism simulation game.",
                },
                {"role": "user", "content": prompt},
            ],
        )
        content = completion.choices[0].message.content
        if not content:
            raise RuntimeError("Ollama returned an empty response")
        return SimulationResponse(response=content, source="ollama")
    except Exception as error:
        logger.warning("Ollama is unavailable; using rule-based logic: %s", error)
        return SimulationResponse(response=rule_based_response(prompt), source="rules")


class RequestHandler(BaseHTTPRequestHandler):
    def _send_json(self, status: int, payload: dict[str, str]) -> None:
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:
        if self.path == "/health":
            self._send_json(200, {"status": "ok"})
        else:
            self._send_json(404, {"error": "not found"})

    def do_POST(self) -> None:
        if self.path != "/generate":
            self._send_json(404, {"error": "not found"})
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))
            request = SimulationRequest.model_validate(
                json.loads(self.rfile.read(length))
            )
        except (ValueError, json.JSONDecodeError, ValidationError) as error:
            logger.info("Invalid generation request: %s", error)
            self._send_json(400, {"error": "request must contain a non-empty prompt"})
            return

        response = generate_response(request.prompt)
        self._send_json(200, response.model_dump())

    def log_message(self, format: str, *args: object) -> None:
        logger.info("%s - %s", self.address_string(), format % args)


if __name__ == "__main__":
    server = ThreadingHTTPServer(("0.0.0.0", AI_ENGINE_PORT), RequestHandler)
    logger.info("AI engine listening on port %s", AI_ENGINE_PORT)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        logger.info("Shutting down AI engine")
        server.server_close()
