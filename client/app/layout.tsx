import type { Metadata } from 'next';
import "./globals.css";
export const metadata: Metadata = {
  title: 'Sample MFlix - MongoDB Movie Database',
  description: 'Explore movies from the MongoDB sample_mflix database',
};
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        {children}
      </body>
    </html>
  );
}