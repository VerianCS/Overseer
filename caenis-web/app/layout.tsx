import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Caenis Overseer — Tactical Observation Deck",
  description:
    "Autonomous telemetry, spatial surveillance, and anomaly diagnostics for PaperMC environments.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full flex flex-col bg-abyss text-foam font-data">
        {children}
      </body>
    </html>
  );
}
