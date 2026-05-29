"""
IIDP 功能冒烟测试脚本

用法:
    python tests/functional/smoke_test.py [--endpoint http://localhost:8060]

测试文件位于 tests/functional/jsonrpc/*.json，每个文件对应一个用户故事。
每个 case 通过 JSON-RPC 2.0 请求调用运行中的 IIDP 应用，断言响应结果。

前置条件:
    docker compose up -d iidp-app  # 应用和依赖服务均 healthy
    pip install requests
"""

import argparse
import json
import pathlib
import sys
import uuid

try:
    import requests
except ImportError:
    print("缺少依赖: pip install requests")
    sys.exit(2)

JSONRPC_PATH = pathlib.Path(__file__).parent / "jsonrpc"


def run(default_endpoint: str) -> int:
    test_files = sorted(JSONRPC_PATH.glob("*.json"))
    if not test_files:
        print(f"未找到测试文件: {JSONRPC_PATH}")
        return 0

    failures = []
    total = 0

    for f in test_files:
        spec = json.loads(f.read_text(encoding="utf-8"))
        endpoint = spec.get("endpoint", default_endpoint)
        story_id = spec.get("storyId", f.stem)

        for case in spec.get("cases", []):
            total += 1
            case_name = case.get("name", f"case-{total}")
            tag = f"{story_id} / {case_name}"

            req = json.loads(json.dumps(case["request"]))
            req["id"] = str(uuid.uuid4())

            try:
                resp = requests.post(endpoint, json=req, timeout=30)
                body = resp.json()
            except Exception as e:
                print(f"FAIL: {tag}  exception={e}")
                failures.append(tag)
                continue

            http_ok = resp.status_code == case.get("expectedHttpStatus", 200)
            result_ok = (not case.get("expectedResult")) or ("result" in body)
            error_ok = (not case.get("expectedError")) or ("error" in body)
            ok = http_ok and result_ok and error_ok

            if ok:
                print(f"PASS: {tag}")
            else:
                detail = json.dumps(body, ensure_ascii=False)
                print(f"FAIL: {tag}  status={resp.status_code}  body={detail}")
                failures.append(tag)

    print(f"\n结果: {total - len(failures)}/{total} 通过，{len(failures)} 失败")
    return 1 if failures else 0


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="IIDP JSON-RPC 冒烟测试")
    parser.add_argument("--endpoint", default="http://localhost:8060", help="应用地址")
    args = parser.parse_args()
    sys.exit(run(args.endpoint))
