export function parseJwt(token: string): any {
  const base64Url: string = token.split('.')[1];
  const base64: string = base64Url.replace('-', '+').replace('_', '/');
  return JSON.parse(Buffer.from(base64, 'base64').toString());
}
